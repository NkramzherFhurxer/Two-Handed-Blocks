package com.nkramzher.twohandedblocks.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Both old and new minecart physics consult ServerPlayer's latest input to
 * nudge a cart. Return zero rider intent while carrying, but do not change
 * momentum, drag, slopes, rails or propulsion.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerMinecartInputMixin {
    @Inject(method = "getLastClientMoveIntent", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$disableMinecartRiderPush(CallbackInfoReturnable<Vec3> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.getVehicle() instanceof AbstractMinecart && BlockRules.isCarrying(player)) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}
