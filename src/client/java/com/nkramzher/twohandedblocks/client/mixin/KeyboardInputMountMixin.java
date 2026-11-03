package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent deliberate steering while the player's main hand is occupied by a
 * two-handed item. Do not zero a vehicle's velocity: powered rails, slopes,
 * currents, pushes and AI must continue to work as in vanilla.
 *
 * Exclude pigs and striders: their controls require dedicated steering items
 * and their natural wandering must not be touched.
 * AbstractNautilus includes normal, zombie and coral zombie nautiluses;
 * their jump-key dash is also blocked, but passive AI and external momentum
 * remain handled by vanilla.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMountMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void twohandedblocks$suppressMountControl(CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !BlockRules.isCarrying(player)) return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractBoat)
                && !(vehicle instanceof AbstractMinecart)
                && !(vehicle instanceof AbstractHorse)
                && !(vehicle instanceof AbstractNautilus)) return;

        // Keep sneaking: the player must still be able to dismount normally.
        boolean dismount = this.keyPresses.shift();
        this.keyPresses = new Input(false, false, false, false, false, dismount, false);
        this.moveVector = Vec2.ZERO;
    }
}
