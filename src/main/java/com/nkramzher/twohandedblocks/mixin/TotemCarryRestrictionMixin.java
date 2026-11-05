package com.nkramzher.twohandedblocks.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a player holds a two-handed block, the other (visually occupied) hand
 * cannot activate its Totem of Undying. The totem remains in that inventory
 * slot, unconsumed. Its usual passive protection returns immediately as soon
 * as the player stops carrying the block.
 *
 * Do not affect mobs, one-handed play, or unrelated item interactions.
 */
@Mixin(LivingEntity.class)
public abstract class TotemCarryRestrictionMixin {
    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$ignoreTotemInOccupiedHand(
            DamageSource killingDamage, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) return;

        InteractionHand carrying = BlockRules.carriedHand(player);
        if (carrying == null) return;

        // Only the inactive hand can hold the totem here. Restrict that
        // particular vanilla item; do not globally disable all
        // DEATH_PROTECTION components from other mods.
        InteractionHand inactive = carrying == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (player.getItemInHand(inactive).is(Items.TOTEM_OF_UNDYING)) {
            // Vanilla continues its normal fatal-damage flow, but cannot
            // consume the totem, show its animation, or apply its effects.
            cir.setReturnValue(false);
        }
    }
}
