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
            poses.rotateDegrees(Axis.XP, -90.0f);
            CarriedModelProfile.Shape profile = CarriedModelProfile.shape(stack);
            poses.rotateDegrees(Axis.YP, CarriedModelProfile.yawDegrees(profile));
            if (CarriedModelProfile.rollDegrees(profile) != 0.0f) {
                poses.rotateDegrees(Axis.ZP, CarriedModelProfile.rollDegrees(profile));
            }
            poses.scale(1.33f, 1.33f, 1.33f);
            item.submit(poses, nodes, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poses.popPose();
        }
        ci.cancel(); // suppress the vanilla off-hand render, not its inventory stack
    }
}
