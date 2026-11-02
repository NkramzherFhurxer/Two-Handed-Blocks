package com.nkramzher.twohandedblocks.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.client.CarriedModelProfile;
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
                // Bring the hands slightly higher and in toward the block sides,
                // instead of making them look like supports underneath it.
                poses.translate(-side * 0.43f, 0.09f, -0.02f);
                poses.rotateDegrees(Axis.YP, side * 32.0f);
                this.renderPlayerArm(poses, nodes, light, inverseHeight, 0.0f, arm, playerState);
                poses.popPose();
            }
        }
        poses.pushPose();
        poses.translate(0.0f, -0.24f, -0.83f);
        // Different model shapes need different orientations. This does not
        // affect item rules and deliberately leaves fence gates untouched.
        CarriedModelProfile.Shape profile = CarriedModelProfile.shape(stack);
        // Level the long bed model across both hands in first person. Rotate
        // around the camera-facing Z axis BEFORE the shape-specific yaw.
        // This is visual-only and affects the sixteen vanilla bed colours.
        if (profile == CarriedModelProfile.Shape.BED) {
            poses.rotateDegrees(Axis.ZP, 32.0f);
        }
        poses.rotateDegrees(Axis.YP, CarriedModelProfile.yawDegrees(profile));
        // Turn only regular vanilla door items by +90° in first person.
        if (CarriedModelProfile.isVanillaDoor(stack)) {
            poses.rotateDegrees(Axis.YP, 90.0f);
        }
        // Specific horizontal turns requested for held minecarts, building
        // stairs, crafting tables and lecterns. First-person only.
        float extraYaw = CarriedModelProfile.firstPersonExtraYawDegrees(stack);
        if (extraYaw != 0.0f) {
            poses.rotateDegrees(Axis.YP, extraYaw);
        }
        if (CarriedModelProfile.rollDegrees(profile) != 0.0f) {
            poses.rotateDegrees(Axis.ZP, CarriedModelProfile.rollDegrees(profile));
        }
        poses.scale(0.90f, 0.90f, 0.90f);
        state.mainHandRenderState.submit(poses, nodes, light, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
        poses.popPose();
        ci.cancel();
    }
}
