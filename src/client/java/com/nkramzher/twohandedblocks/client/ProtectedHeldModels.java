package com.nkramzher.twohandedblocks.client;

import com.nkramzher.twohandedblocks.BlockRules;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Selects a dedicated, bundled item model for *visual rendering only*.
 *
 * Never mutate the inventory's ItemStack or its persistent ITEM_MODEL component.
 * The render copy is used strictly for our two-handed F1/F5 render states,
 * while item behavior, inventory, placed blocks, GUIs, and thrown items are
 * unaffected. Textures referenced by the models may still be replaced by
 * other resource packs, while our dedicated geometry and display transforms
 * remain in the twohandedblocks namespace.
 */
public final class ProtectedHeldModels {
    private ProtectedHeldModels() {}

    // Limited to models that are shipped together with our customized
    // Block-Accurate v33 pack. Unlisted items retain the established vanilla
    // rendering path to avoid missing-model regressions.
    private static final Set<String> PROTECTED = Set.of(
            "bell", "decorated_pot", "cake", "hopper", "cauldron",
            "acacia_boat", "acacia_chest_boat", "bamboo_raft",
            "bamboo_chest_raft", "birch_boat", "birch_chest_boat",
            "cherry_boat", "cherry_chest_boat", "dark_oak_boat",
            "dark_oak_chest_boat", "jungle_boat", "jungle_chest_boat",
            "mangrove_boat", "mangrove_chest_boat",
            "oak_boat", "oak_chest_boat", "pale_oak_boat",
            "pale_oak_chest_boat", "poplar_boat", "poplar_chest_boat",
            "spruce_boat", "spruce_chest_boat",
            "minecart", "chest_minecart", "command_block_minecart",
            "furnace_minecart", "hopper_minecart", "tnt_minecart",
            "acacia_door", "bamboo_door", "birch_door", "cherry_door",
            "crimson_door", "dark_oak_door", "iron_door", "jungle_door",
            "mangrove_door", "oak_door", "pale_oak_door",
            "poplar_door", "spruce_door", "warped_door",
            "copper_door", "exposed_copper_door", "weathered_copper_door",
            "oxidized_copper_door", "waxed_copper_door",
            "waxed_exposed_copper_door", "waxed_weathered_copper_door",
            "waxed_oxidized_copper_door",
            "campfire", "soul_campfire",
            "white_bed", "orange_bed", "magenta_bed", "light_blue_bed",
            "yellow_bed", "lime_bed", "pink_bed", "gray_bed",
            "light_gray_bed", "cyan_bed", "purple_bed", "blue_bed",
            "brown_bed", "green_bed", "red_bed", "black_bed", "straw_bed"
    );

    public static ItemStack visualCopy(ItemStack original) {
        if (!BlockRules.isCarriedBlock(original)) return original;
        Identifier itemId = BuiltInRegistries.ITEM.getKey(original.getItem());
        if (itemId == null || !"minecraft".equals(itemId.getNamespace())
                || !PROTECTED.contains(itemId.getPath())) {
            return original;
        }

        ItemStack visual = original.copy();
        visual.set(DataComponents.ITEM_MODEL,
                Identifier.fromNamespaceAndPath("twohandedblocks",
                        "held/" + itemId.getPath()));
        return visual;
    }
}
