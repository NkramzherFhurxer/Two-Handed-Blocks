package com.nkramzher.twohandedblocks;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Central rule shared by the pose renderer and the interaction restrictions.
 *  The exceptions concern small PLACEABLE blocks, not non-block items.
 */
public final class BlockRules {
    private BlockRules() {}

    // Explicit two-handed overrides always win, even if a naming pattern changes.
    private static final Set<String> TWO_HANDS = Set.of(
            "cactus", "nether_brick_fence", "tinted_glass",
            "mossy_cobblestone_wall", "cobblestone_wall", "bamboo_block", "stripped_bamboo_block",
            "big_dripleaf", "small_dripleaf", "tall_seagrass",
            "rose_bush", "peony"
    );

    // Individual small blocks that are carried naturally in one hand.
    private static final Set<String> ONE_HAND = Set.of(
            // All seven vanilla heads are small decorative/wearable items.
            // Keep the dragon head in the same family for consistent handling.
            "dragon_head", "skeleton_skull", "wither_skeleton_skull", "zombie_head",
            "creeper_head", "piglin_head", "player_head",
            "light", "barrier", "structure_void",
            "torch", "soul_torch", "redstone_torch", "lantern", "soul_lantern",
            "lever", "tripwire_hook", "lightning_rod", "ladder", "chain", "iron_chain", "flower_pot", "iron_bars", "glass_pane", "red_shrub", "shelf_mushroom",
            "repeater", "comparator", "daylight_detector", "redstone_wire",
            "redstone", "lily_pad", "snow", "sea_pickle", "cobweb",
            "vine", "glow_lichen", "hanging_roots", "short_grass", "fern",
            "dead_bush", "bush", "firefly_bush", "leaf_litter", "wildflowers",
            "pink_petals", "dry_grass", "tall_dry_grass", "dandelion", "poppy",
            "blue_orchid", "allium", "azure_bluet", "oxeye_daisy", "cornflower",
            "lily_of_the_valley", "wither_rose", "torchflower", "open_eyeblossom",
            "closed_eyeblossom", "brown_mushroom", "red_mushroom", "crimson_fungus",
            "warped_fungus", "crimson_roots", "warped_roots", "nether_sprouts",
            "bamboo", "sugar_cane", "cocoa_beans", "cactus_flower",
            "azalea", "flowering_azalea", "small_amethyst_bud", "medium_amethyst_bud",
            "large_amethyst_bud", "amethyst_cluster", "pointed_dripstone",
            "sculk_vein", "spore_blossom", "hanging_sign", "suspicious_stew",
            "resin_clump", "turtle_egg", "sniffer_egg", "frogspawn",
            "kelp", "seagrass", "weeping_vines", "twisting_vines",
            "pitcher_plant", "pitcher_pod", "torchflower_seeds", "sunflower", "lilac",
            "tall_grass", "large_fern", "mangrove_propagule", "short_dry_grass",
            "pale_hanging_moss", "sulfur_spike", "end_rod", "tripwire", "string",
            "nether_wart", "wheat_seeds", "beetroot_seeds", "pumpkin_seeds",
            "melon_seeds", "sweet_berries", "glow_berries", "carrot", "potato"
    );

    private static boolean oneHand(String path) {
        if (ONE_HAND.contains(path)) return true;
        // All vanilla color/material variants of these small objects.
        return path.endsWith("_torch")
                || path.endsWith("_button")
                || path.endsWith("_pressure_plate")
                || path.endsWith("_carpet")
                || path.endsWith("_candle")
                || path.endsWith("_rail") || path.equals("rail")
                || path.endsWith("_sign")
                || path.endsWith("_hanging_sign")
                || path.endsWith("_sapling")
                || path.endsWith("_coral")
                || path.endsWith("_coral_fan")
                || path.endsWith("_wall_fan")
                || path.endsWith("_banner")
                || path.endsWith("_cushion")
                || path.endsWith("_trapdoor")
                || path.endsWith("_shelf")
                || path.endsWith("_stained_glass_pane")
                || path.endsWith("_shelf_fungus")
                || path.endsWith("_mushroom")
                || path.endsWith("_petals")
                || path.endsWith("_leaves_carpet")
                || path.endsWith("copper_chain")
                || path.endsWith("copper_lantern");
    }

    private static boolean twoHands(String path) {
        return TWO_HANDS.contains(path)
                || path.endsWith("_fence")
                || path.endsWith("_fence_gate")
                || path.endsWith("_wall");
    }

    private static boolean isVanillaVehicle(String path) {
        return path.endsWith("_boat") || path.endsWith("_chest_boat")
                || path.equals("bamboo_raft") || path.equals("bamboo_chest_raft")
                || path.equals("minecart") || path.endsWith("_minecart");
    }

    public static boolean isCarriedBlock(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        // String is an item; when placed it becomes tripwire. Never use the two-hand pose.
        if ("minecraft".equals(id.getNamespace()) && "string".equals(id.getPath())) return false;
        // Boats (including chest boats and bamboo rafts) and all minecart variants
        // are placeable vehicles, not BlockItems. Carry vanilla vehicles in both hands.
        // Keep the namespace check so unrelated modded items are not captured.
        if ("minecraft".equals(id.getNamespace()) && isVanillaVehicle(id.getPath())) return true;
        if (!(stack.getItem() instanceof BlockItem)) return false;
        if (!"minecraft".equals(id.getNamespace())) return true; // other mods: default to two hands
        String path = id.getPath();
        if (twoHands(path)) return true;
        return !oneHand(path);
    }

    public static boolean isCarrying(Player player) {
        return player != null && !player.isSpectator() && isCarriedBlock(player.getMainHandItem());
    }
}
