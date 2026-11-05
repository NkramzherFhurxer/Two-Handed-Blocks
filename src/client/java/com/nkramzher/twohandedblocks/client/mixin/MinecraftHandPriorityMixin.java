package com.nkramzher.twohandedblocks.client.mixin;

import com.nkramzher.twohandedblocks.BlockRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla checks the main hand first on right-click. When a two-handed block
 * is carried, try only its selected hand so that an offhand block can be
 * placed even when the main hand holds a usable one-handed item.
 *
 * In ordinary one-hand gameplay, preserve the vanilla hand order exactly.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftHandPriorityMixin {
    @Redirect(method = "startUseItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand[] twohandedblocks$selectCarryingHand() {
        InteractionHand carrying = BlockRules.carriedHand(Minecraft.getInstance().player);
        return carrying == null
                ? InteractionHand.values()
                : new InteractionHand[]{carrying};
    }
}
