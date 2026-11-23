package com.nkramzher.twohandedblocks.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.client.CarriedModelProfile;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw the block at the centre of the torso and hide the secondary item. */
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin extends ItemInHandLayer<AvatarRenderState, PlayerModel> {
    private static final float PALM_FROM_SHOULDER = 10.0f / 16.0f;
    // This is a guardrail, not a sitting offset. Large incompatible animation
    // transforms fall back to the last stable carry pose instead of hiding the block.
    private static final float MAX_HAND_FOLLOW = 0.35f;

    protected PlayerItemInHandLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    private static Vector3f palmPosition(PlayerModel model, AvatarRenderState state, HumanoidArm arm) {
        PoseStack p = new PoseStack();
        // Use the same hand anchor method as Minecraft's native held-item layer.
        // This allows compatible animation mods to contribute their own pivots.
        model.translateToHand(state, arm, p);
        return p.last().pose().transformPosition(0.0f, PALM_FROM_SHOULDER, 0.0f, new Vector3f());
    }

    private static Vector3f handMidpoint(PlayerModel model, AvatarRenderState state) {
        return palmPosition(model, state, HumanoidArm.RIGHT)
                .add(palmPosition(model, state, HumanoidArm.LEFT))
                .mul(0.5f);
    }

    private static void setCanonicalCarry(ModelPart arm, HumanoidArm side) {
        arm.setPos(side == HumanoidArm.RIGHT ? -5.0f : 5.0f, 2.0f, 0.0f);
        arm.setRotation(-1.28f, side == HumanoidArm.RIGHT ? -0.12f : 0.12f,
                side == HumanoidArm.RIGHT ? -0.18f : 0.18f);
    }

    /**
     * Compare the live palms to the Hotfix 7 carry pose in the SAME model,
     * frame and CEM configuration. There is no sit/leg-angle heuristic.
     * Transform the difference back to the model-root coordinate frame because
     * root translation/rotation is applied separately to the block.
     * The original animation state is always restored before item rendering.
     */
    private static Vector3f handFollowDelta(PlayerModel model, AvatarRenderState state) {
        Vector3f live = handMidpoint(model, state);

        ModelPart right = model.rightArm;
        ModelPart left = model.leftArm;
        float[] r = {right.x, right.y, right.z, right.xRot, right.yRot, right.zRot};
        float[] l = {left.x, left.y, left.z, left.xRot, left.yRot, left.zRot};
        Vector3f reference;
        try {
            setCanonicalCarry(right, HumanoidArm.RIGHT);
            setCanonicalCarry(left, HumanoidArm.LEFT);
            reference = handMidpoint(model, state);
        } finally {
            right.setPos(r[0], r[1], r[2]);
            right.setRotation(r[3], r[4], r[5]);
            left.setPos(l[0], l[1], l[2]);
            left.setRotation(l[3], l[4], l[5]);
        }

        Vector3f delta = live.sub(reference);
        // The reference above includes root transforms; remove them here.
        PoseStack rootPose = new PoseStack();
        model.root().translateAndRotate(rootPose);
        new Matrix4f(rootPose.last().pose()).invert().transformDirection(delta);
        if (!Float.isFinite(delta.x()) || !Float.isFinite(delta.y()) || !Float.isFinite(delta.z())) {
            return new Vector3f();
        }

        float len = delta.length();
        if (len > MAX_HAND_FOLLOW) delta.mul(MAX_HAND_FOLLOW / len);
        return delta;
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$submitBlock(AvatarRenderState state, ItemStackRenderState item,
            ItemStack stack, HumanoidArm arm, PoseStack poses, SubmitNodeCollector nodes,
            int light, CallbackInfo ci) {
        ItemStack selected = state.mainArm == HumanoidArm.RIGHT
                ? state.rightHandItemStack : state.leftHandItemStack;
        if (state.isSpectator || !BlockRules.isCarriedBlock(selected)) return;

        if (arm == state.mainArm) {
            poses.pushPose();
            PlayerModel animatedModel = this.getParentModel();
            Vector3f handDelta = handFollowDelta(animatedModel, state);
            // The player's animated root (lean/sit/turn) also moves its arms.
            // HF7 omitted this transform for held blocks. Usually it is identity.
            animatedModel.root().translateAndRotate(poses);
            // Model coordinates are +Y down. Center the carried item between
            // both arms. The model's baked third-person pivot is different for
            // full blocks / fence gates than for fence / wall models.
            // Preserve the proven fence / wall alignment rather than applying
            // one universal X offset to every model.
            CarriedModelProfile.Shape profile = CarriedModelProfile.shape(stack);
            poses.translate(CarriedModelProfile.thirdPersonHorizontalOffset(stack) + handDelta.x(),
                    0.40f + handDelta.y(), -0.53f + handDelta.z());
            if (profile == CarriedModelProfile.Shape.BELL) {
                // The bell has a special item model: keep the last tested pose
                // until we deliberately revisit its geometry.
                poses.rotateDegrees(Axis.XP, -90.0f);
                poses.rotateDegrees(Axis.YP, CarriedModelProfile.yawDegrees(profile));
                poses.rotateDegrees(Axis.ZP, CarriedModelProfile.rollDegrees(profile));
            } else {
                // The prior -45 Y / -75 X compensation successfully made
                // full blocks face forward in F5, but rendered their top faces
                // at the bottom (visually upside down). Turn the held model
                // through 180 degrees around the screen-facing Z axis BEFORE
                // cancelling the hand-model pitch/yaw, keeping the front face
                // flat while making the top point upward.
                // Keep the successful sideways tilt (-20 X) and refine only
                // the screen-plane angle: the character's left-hand side
                // (screen-right in front-facing F5) was slightly too low.
                // The center offset and first-person bed pose remain unchanged.
                if (profile == CarriedModelProfile.Shape.BED) {
                    poses.rotateDegrees(Axis.XP, -20.0f);
                    poses.rotateDegrees(Axis.ZP, 43.0f);
                }
                poses.rotateDegrees(Axis.ZP, 180.0f);

                // Turn only regular vanilla doors by +90° in third person.
                // Trapdoors and fence gates retain their existing alignment.
                if (CarriedModelProfile.isVanillaDoor(stack)) {
                    poses.rotateDegrees(Axis.YP, 90.0f);
                }

                // Present the latches of all eleven vanilla chest variants toward
                // the viewer in F5, like the front of furnaces. The chests use a
                // special renderer with a different model-facing convention.
                // No offset changes and no minecart rendering changes.
                if (CarriedModelProfile.isVanillaChest(stack)) {
                    poses.rotateDegrees(Axis.YP, 270.0f);
                }

                // Fence and wall item models are edge-on with the normal cube
                // orientation. Rotate only this family a quarter turn to show
                // the horizontal connectors, as with a fence gate. The gate
                // itself remains untouched by this family-specific extra yaw.
                if (profile == CarriedModelProfile.Shape.FENCE_OR_WALL) {
                    poses.rotateDegrees(Axis.YP, 90.0f);
                }

                // Cancel the default third-person model transform. The 1st-
                // person rendering path is deliberately not modified here.
                poses.rotateDegrees(Axis.YP, -45.0f);
                poses.rotateDegrees(Axis.XP, -75.0f);
            }
            poses.scale(1.33f, 1.33f, 1.33f);
            item.submit(poses, nodes, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poses.popPose();
        }
        ci.cancel(); // suppress the vanilla off-hand render, not its inventory stack
    }
}
