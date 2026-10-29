package com.nkramzher.twohandedblocks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Only block items in the selected main-hand slot activate the two-handed pose. */
public final class BlockRules {
    private BlockRules() {}

    public static boolean isCarriedBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    public static boolean isCarrying(Player player) {
        return player != null && !player.isSpectator() && isCarriedBlock(player.getMainHandItem());
    }
}
