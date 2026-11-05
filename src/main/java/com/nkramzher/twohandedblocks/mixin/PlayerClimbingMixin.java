package com.nkramzher.twohandedblocks.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A player carrying a two-handed block cannot use hand-powered climbing:
 * ladders, scaffolding, overworld vines, cave vines or Nether vines.
 *
 * Vanilla climbing uses LivingEntity.onClimbable for all these blocks,
 * including the corresponding special trapdoor/ladder combination.
 * Scaffolding still permits upward hops without its climbing assist, so
 * stop those jumps separately while physically inside/on scaffolding.
 *
 * Ordinary walking, ground jumps, water flow, soul-sand bubble columns,
 * external forces and the mobility of mobs remain untouched.
 */
@Mixin(LivingEntity.class)
public abstract class PlayerClimbingMixin {
    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$preventHandPoweredClimbing(
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && BlockRules.isCarrying(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$preventScaffoldingHops(CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !BlockRules.isCarrying(player)) {
            return;
        }

        // Disallow jumping upward through scaffolding one level at a time
        // after its normal climbable assistance has been disabled.
        // Do not affect normal jumping near a ladder/vine or on solid ground.
        if (player.level().getBlockState(player.blockPosition()).is(Blocks.SCAFFOLDING)
                || player.level().getBlockState(player.blockPosition().below())
                        .is(Blocks.SCAFFOLDING)) {
            ci.cancel();
        }
    }
}
