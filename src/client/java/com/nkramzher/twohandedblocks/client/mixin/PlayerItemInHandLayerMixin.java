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
            // Model coordinates are +Y down. Place the object between the two elbows.
            poses.translate(0.0f, 0.40f, -0.53f);
            CarriedModelProfile.Shape profile = CarriedModelProfile.shape(stack);
            if (profile == CarriedModelProfile.Shape.BELL) {
                // The bell has a special item model: keep the last tested pose
                // until we deliberately revisit its geometry.
                poses.rotateDegrees(Axis.XP, -90.0f);
                poses.rotateDegrees(Axis.YP, CarriedModelProfile.yawDegrees(profile));
                poses.rotateDegrees(Axis.ZP, CarriedModelProfile.rollDegrees(profile));
            } else {
                // The item was already resolved for THIRD_PERSON_*_HAND by Minecraft.
                // Vanilla block item models generally add a 75-degree X pitch
                // followed by a 45-degree Y yaw, so adding our former -90/135
                // rotated an ALREADY ROTATED item (diagonal and tilted in F5).
                // Reverse the model's hand-view rotation in reverse order,
                // leaving the model upright and front-facing in the carried pose.
                // First-person uses its own mixin and is entirely unaffected.
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
