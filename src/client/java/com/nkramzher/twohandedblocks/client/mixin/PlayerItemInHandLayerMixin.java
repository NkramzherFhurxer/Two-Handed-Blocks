package com.nkramzher.twohandedblocks.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.client.CarriedModelProfile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw the block at the centre of the torso and hide the secondary item. */
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$submitBlock(AvatarRenderState state, ItemStackRenderState item,
            ItemStack stack, HumanoidArm arm, PoseStack poses, SubmitNodeCollector nodes,
            int light, CallbackInfo ci) {
        ItemStack main = state.mainArm == HumanoidArm.RIGHT
                ? state.rightHandItemStack : state.leftHandItemStack;
        ItemStack offhand = state.mainArm == HumanoidArm.RIGHT
                ? state.leftHandItemStack : state.rightHandItemStack;
        InteractionHand carrying = BlockRules.carriedHand(main, offhand);
        if (state.isSpectator || carrying == null) return;
        HumanoidArm carryingArm = carrying == InteractionHand.MAIN_HAND
                ? state.mainArm : state.mainArm.getOpposite();

        if (arm == carryingArm) {
            poses.pushPose();
            // Model coordinates are +Y down. Center the carried item between
            // both arms. The model's baked third-person pivot is different for
            // full blocks / fence gates than for fence / wall models.
            // Preserve the proven fence / wall alignment rather than applying
            // one universal X offset to every model.
            CarriedModelProfile.Shape profile = CarriedModelProfile.shape(stack);
            // Offhand two-handed models now use the same main-hand model
            // context, so all carried blocks share these exact F5 offsets.
            poses.translate(CarriedModelProfile.thirdPersonHorizontalOffset(stack),
                    0.40f, -0.53f);
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
