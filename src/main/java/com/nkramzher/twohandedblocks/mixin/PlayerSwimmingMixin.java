package com.nkramzher.twohandedblocks.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppress only the horizontal sprint-swimming state while a player carries
 * a two-handed item in water. Water entry, buoyancy, jumping, vertical motion,
 * air supply and ordinary movement are left to vanilla.
 *
 * Runs on both the client and the logical server, so the swimming flag and
 * player pose agree. Land-based movement/crawling is not intercepted.
 */
@Mixin(Player.class)
public abstract class PlayerSwimmingMixin {
    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$preventSwimmingWithFullHands(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.isInWater() && BlockRules.isCarrying(player)) {
            // Sprint and swimming are separate flags. Stop both; otherwise
            // Ctrl or double-tap forward can still speed up underwater travel.
            player.setSprinting(false);
            player.setSwimming(false);
            ci.cancel();
        }
    }
}
