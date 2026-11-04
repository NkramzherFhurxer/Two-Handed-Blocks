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
 * Both hands share the selected MAIN-hand model orientation when a two-handed
 * block is held in the offhand. This changes only the baked model display
 * context, not the item, slot, arm movement, collision or placement rules.
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
        if (!(entity instanceof Player player)
                || BlockRules.carriedHand(player) != InteractionHand.OFF_HAND
                || item != player.getOffhandItem()) {
            return;
        }

        HumanoidArm mainArm = player.getMainArm();
        ItemDisplayContext reference = switch (context) {
            case FIRST_PERSON_LEFT_HAND ->
                    mainArm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : null;
            case FIRST_PERSON_RIGHT_HAND ->
                    mainArm == HumanoidArm.LEFT ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : null;
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
