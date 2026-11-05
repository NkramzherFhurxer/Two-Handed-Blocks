package com.nkramzher.twohandedblocks.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.client.CarriedModelProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
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
        var player = Minecraft.getInstance().player;
        if (BlockRules.isCarrying(player)) {
            state.handRenderSelection =
                    FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        }

        // F1 is extracted separately by FirstPersonHandsAndItems, which
        // resolves its own ItemStackRenderState before this renderer runs.
        // Unlike F5 this path need not call ItemModelResolver.updateForLiving.
        // Replace ONLY the offhand display state with the primary-hand
        // context here, using the exact cached stack actually drawn in F1.
        // F5 uses a different render state and is completely unaffected.
        if (player != null && BlockRules.carriedHand(player) == InteractionHand.OFF_HAND
                && BlockRules.isCarriedBlock(state.offHandItem)) {
            ItemDisplayContext context = player.getMainArm() == HumanoidArm.RIGHT
                    ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                    : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                    state.offHandRenderState,
                    state.offHandItem,
                    context,
                    player.level(),
                    player,
                    player.getId() + context.ordinal());
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
        // Keep the original stable arm pose for every block, including
        // the decorated pot. Pot-only arm translations in Hotfixes 14-16
        // made the arms look unnaturally elongated in first person.
        // Fine tuning the pot is now done with the resource-pack model.
        if (!playerState.avatarRenderState.isInvisible) {
            for (HumanoidArm arm : HumanoidArm.values()) {
                poses.pushPose();
                float side = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
                poses.translate(-side * 0.43f, 0.09f, -0.02f);
                poses.rotateDegrees(Axis.YP, side * 32.0f);
                this.renderPlayerArm(poses, nodes, light, inverseHeight, 0.0f, arm, playerState);
                poses.popPose();
            }
        }
        poses.pushPose();
        // The model context is normalized before rendering, so either hand
        // reuses the main-hand's carefully calibrated F1 transforms.
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
