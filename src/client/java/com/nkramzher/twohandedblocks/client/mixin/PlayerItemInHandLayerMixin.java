package com.nkramzher.twohandedblocks.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.client.CarriedModelProfile;
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
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw the block at the centre of the torso and hide the secondary item. */
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin extends ItemInHandLayer<AvatarRenderState, PlayerModel> {
    // Mean position of the palms in our proven two-handed idle arm pose.
    // Shoulder pivots at +/-5,2,0 pixels, palm at y=10px, arm rotations
    // x=-1.28, y=+/-0.12, z=+/-0.18 (see PlayerModelMixin).
    private static final float REST_HAND_Y = 0.2884692f;
    private static final float REST_HAND_Z = -0.594454f;
    private static final float PALM_Y = 10.0f / 16.0f;

    protected PlayerItemInHandLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    /** Translate through the actual animated arm to find its palm in model space. */
    private static Vector3f palm(PlayerModel model, AvatarRenderState state, HumanoidArm arm) {
        PoseStack handPose = new PoseStack();
        model.translateToHand(state, arm, handPose);
        return handPose.last().pose().transformPosition(0.0f, PALM_Y, 0.0f, new Vector3f());
    }

    /**
     * Sitting emotes bend both legs forward, unlike walking/running where
     * their pitch has opposite signs. Exclude riding to preserve mounts.
     * Emote-based sitting has no vanilla render-state boolean of its own.
     */
    private static boolean looksSeated(AvatarRenderState state, PlayerModel model) {
        return !state.isPassenger && model.leftLeg.xRot < -0.9f && model.rightLeg.xRot < -0.9f;
    }

    /**
     * Apply only the difference from the already-calibrated standing pose.
     * This preserves the size, angles and offsets for every block while
     * making a sitting block follow the animated midpoint between the palms.
     */
    private Vector3f seatedHandCorrection(AvatarRenderState state) {
        PlayerModel model = this.getParentModel();
        if (!looksSeated(state, model)) return new Vector3f();
        Vector3f right = palm(model, state, HumanoidArm.RIGHT);
        Vector3f left = palm(model, state, HumanoidArm.LEFT);
        return right.add(left).mul(0.5f).sub(0.0f, REST_HAND_Y, REST_HAND_Z);
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
            // Model coordinates are +Y down. Center the carried item between
            // both arms. The model's baked third-person pivot is different for
            // full blocks / fence gates than for fence / wall models.
            // Preserve the proven fence / wall alignment rather than applying
            // one universal X offset to every model.
            CarriedModelProfile.Shape profile = CarriedModelProfile.shape(stack);
            Vector3f seatedDelta = seatedHandCorrection(state);
            poses.translate(CarriedModelProfile.thirdPersonHorizontalOffset(stack) + seatedDelta.x(),
                    0.40f + seatedDelta.y(), -0.53f + seatedDelta.z());
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
