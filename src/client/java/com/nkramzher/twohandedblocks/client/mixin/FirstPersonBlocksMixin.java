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

    // Always submit the selected carrying hand even if a main-hand item would
    // normally suppress offhand rendering (e.g. vanilla bow/map poses).
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void twohandedblocks$showCarriedHand(float partialTicks, PoseStack poses,
            SubmitNodeCollector nodes, PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        if (BlockRules.isCarrying(Minecraft.getInstance().player)) {
            state.handRenderSelection =
                    FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        }
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$renderBlock(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot,
            InteractionHand hand, float attack, ItemStack stack, float inverseHeight,
            PoseStack poses, SubmitNodeCollector nodes, int light, CallbackInfo ci) {
        InteractionHand carrying = BlockRules.carriedHand(Minecraft.getInstance().player);
        if (carrying == null) return;
        if (hand != carrying) {
            ci.cancel(); // Hide whichever item is not holding the two-hand block.
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
        // Small first-person horizontal (camera-space Y) adjustment for beds:
        // the pink foot stays at screen-left but moves closer to the player,
        // while the white pillow stays at screen-right and moves farther away.
        // Apply before the existing -24 Z leveling and bed item yaw: do not
        // alter the approved level pose, scale, centering or third person view.
        if (profile == CarriedModelProfile.Shape.BED) {
            poses.rotateDegrees(Axis.YP, 30.0f);
            poses.rotateDegrees(Axis.ZP, -24.0f);
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
        if (carrying == InteractionHand.MAIN_HAND) {
            state.mainHandRenderState.submit(poses, nodes, light, OverlayTexture.NO_OVERLAY, 0);
        } else {
            state.offHandRenderState.submit(poses, nodes, light, OverlayTexture.NO_OVERLAY, 0);
        }
        poses.popPose();
        poses.popPose();
        ci.cancel();
    }
}
