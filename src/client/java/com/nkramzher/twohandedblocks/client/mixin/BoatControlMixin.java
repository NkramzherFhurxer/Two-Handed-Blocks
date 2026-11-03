package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extra boat safeguard: zero only paddle commands, not the current movement.
 * Includes chest boats and bamboo rafts through AbstractBoat.
 */
@Mixin(AbstractBoat.class)
public abstract class BoatControlMixin {
    @Shadow private boolean inputLeft;
    @Shadow private boolean inputRight;
    @Shadow private boolean inputUp;
    @Shadow private boolean inputDown;

    @Inject(method = "setInput", at = @At("HEAD"), cancellable = true)
    private void twohandedblocks$disablePaddlingWithFullHands(
            boolean left, boolean right, boolean up, boolean down, CallbackInfo ci) {
        AbstractBoat boat = (AbstractBoat) (Object) this;
        if (boat.getControllingPassenger() instanceof Player rider
                && rider.getVehicle() == boat && BlockRules.isCarrying(rider)) {
            this.inputLeft = false;
            this.inputRight = false;
            this.inputUp = false;
            this.inputDown = false;
            ci.cancel();
        }
    }
}
