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
                poses.rotateDegrees(Axis.ZP, 180.0f);

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
