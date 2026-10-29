package com.nkramzher.twohandedblocks.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Experimental first-person two-arm view for block items. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonBlocksMixin {
    @Shadow
    private void renderPlayerArm(PoseStack poses, SubmitNodeCollector nodes, int light,
            float inverseHeight, float attack, HumanoidArm arm, PlayerRenderState playerState) {
        throw new AssertionError("Mixin shadow was not applied");
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$renderBlock(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot,
            InteractionHand hand, float attack, ItemStack stack, float inverseHeight,
            PoseStack poses, SubmitNodeCollector nodes, int light, CallbackInfo ci) {
        if (!BlockRules.isCarrying(Minecraft.getInstance().player)) return;
        if (hand == InteractionHand.OFF_HAND) {
            ci.cancel(); // hide the occupied secondary hand
            return;
        }
        if (!BlockRules.isCarriedBlock(stack) || state.isScoping || playerState.avatarRenderState == null) return;

        poses.pushPose();
        if (!playerState.avatarRenderState.isInvisible) {
            // First-person arms behind the block, in a symmetrical holding pose.
            for (HumanoidArm arm : HumanoidArm.values()) {
                poses.pushPose();
                float side = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
                poses.translate(-side * 0.33f, -0.1f, 0.08f);
                poses.rotateDegrees(Axis.YP, side * 18.0f);
                this.renderPlayerArm(poses, nodes, light, inverseHeight, 0.0f, arm, playerState);
                poses.popPose();
            }
        }
        poses.pushPose();
        poses.translate(0.0f, -0.29f, -0.60f);
        poses.rotateDegrees(Axis.YP, 180.0f);
        poses.scale(1.25f, 1.25f, 1.25f);
        state.mainHandRenderState.submit(poses, nodes, light, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
        poses.popPose();
        ci.cancel();
    }
}
