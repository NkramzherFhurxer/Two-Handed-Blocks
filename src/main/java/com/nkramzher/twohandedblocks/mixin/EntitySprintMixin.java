package com.nkramzher.twohandedblocks.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stop new sprint attempts only when a player carries a two-handed item in
 * water. Intercept the sprint state itself rather than the sprint key binding,
 * so remapped keys, double-tapping forward and toggle sprint all obey it.
 *
 * Ordinary sprinting on land and vanilla sprinting with one-handed items
 * remain unchanged, as do other entities and passenger movement.
 */
@Mixin(Entity.class)
public abstract class EntitySprintMixin {
    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$preventWaterSprinting(boolean sprinting, CallbackInfo ci) {
        if (sprinting && (Object) this instanceof Player player
                && player.isInWater() && BlockRules.isCarrying(player)) {
            ci.cancel();
        }
    }
}
