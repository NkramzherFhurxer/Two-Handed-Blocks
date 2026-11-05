package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third-person offhand two-handed blocks share the selected MAIN-hand model
 * orientation. First-person models are resolved separately when F1 renders.
 * This changes no inventory, movement, collision or placement rules.
 *
 * The original item transforms and model overrides (including resource packs)
 * are applied as though the block were rendered from the player's primary hand.
 * Left-handed players use their left main-hand model as the reference instead.
 */
@Mixin(ItemModelResolver.class)
public abstract class CarriedItemModelContextMixin {

    @Inject(method = "updateForLiving", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$useMainHandModelForOffhandBlock(
            ItemStackRenderState output, ItemStack item, ItemDisplayContext context,
            LivingEntity entity, CallbackInfo ci) {
        // Third-person was confirmed working in Hotfix 10: keep its original
        // exact-stack matching behavior, and do not handle first-person here.
        // The first-person path is normalized directly in
        // FirstPersonBlocksMixin, after its independent state extraction.
        if (!(entity instanceof Player player)
                || BlockRules.carriedHand(player) != InteractionHand.OFF_HAND
                || item != player.getOffhandItem()) {
            return;
        }

        HumanoidArm mainArm = player.getMainArm();
        ItemDisplayContext reference = switch (context) {
            case THIRD_PERSON_LEFT_HAND ->
                    mainArm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : null;
            case THIRD_PERSON_RIGHT_HAND ->
                    mainArm == HumanoidArm.LEFT ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : null;
            default -> null;
        };

        // Leave inventory/GUI, world items, head, shelves, and ordinary
        // one-handed items unchanged. Never modify vanilla's main-hand state.
        if (reference == null) return;

        // updateForLiving normally delegates to updateForTopItem with this seed.
        // Rebuild once with the selected primary-hand context; no recursion.
        ((ItemModelResolver) (Object) this).updateForTopItem(
                output, item, reference, entity.level(), entity,
                entity.getId() + reference.ordinal());
        ci.cancel();
    }
}
