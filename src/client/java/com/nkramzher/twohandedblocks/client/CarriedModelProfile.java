package com.nkramzher.twohandedblocks.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Local, vanilla-only visual presets. The item model's own hand transform is
 * still applied by Minecraft; these angles tune our additional carrying pose.
 * Never use these presets to alter interaction rules or inventory items.
 */
public final class CarriedModelProfile {
    private CarriedModelProfile() {}

    public enum Shape {
        DEFAULT, FENCE_OR_WALL, BED, BELL, HEAVY_CORE
    }

    public static Shape shape(ItemStack item) {
        if (item.isEmpty()) return Shape.DEFAULT;
        Identifier id = BuiltInRegistries.ITEM.getKey(item.getItem());
        if (!"minecraft".equals(id.getNamespace())) return Shape.DEFAULT;
        String name = id.getPath();
        if (name.equals("heavy_core")) return Shape.HEAVY_CORE;
        if (name.equals("bell")) return Shape.BELL;
        if (name.endsWith("_bed")) return Shape.BED;
        // Fence gates are intentionally excluded: the user confirmed they
        // already have the desired presentation in Hotfix 1.
        if (name.endsWith("_fence") || name.endsWith("_wall"))
            return Shape.FENCE_OR_WALL;
        return Shape.DEFAULT;
    }

    public static float yawDegrees(Shape profile) {
        return switch (profile) {
            case FENCE_OR_WALL -> 45.0f; // expose the connecting sections
            case BED -> 90.0f;           // keep the bed laid out horizontally
            case HEAVY_CORE -> 180.0f;   // restore the previous core orientation
            case BELL, DEFAULT -> 135.0f;
        };
    }

    /**
     * Third-person horizontal offsets, calibrated against user screenshots.
     * Standard cubes and fence gates were left of the torso in F5 after HF4;
     * fence and wall models were already aligned. The bell keeps its prior
     * separate placement until the bell's model is investigated.
     */
    public static float thirdPersonHorizontalOffset(Shape profile) {
        return switch (profile) {
            case FENCE_OR_WALL -> -0.15f; // Already perfectly centered; do not move.
            case BELL -> -0.16f;          // Tiny correction; detailed bell pose later.
            default -> 0.155f;           // Small extra shift toward screen-left; fences and walls unchanged.
        };
    }

    /**
     * The layer renders an ItemStack, not a Shape. This overload fixes the
     * compile error and lets you fine-tune individual vanilla items later.
     * Uncomment a case and change its float to override just that item.
     */
    public static float thirdPersonHorizontalOffset(ItemStack stack) {
        if (stack.isEmpty()) return thirdPersonHorizontalOffset(Shape.DEFAULT);
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null && "minecraft".equals(id.getNamespace())) {
            // Vanilla doors: dedicated third-person offset for the 3D door models.
            // Keep their 90-degree rotation and leave trapdoors/fence gates untouched.
            if (isVanillaDoor(stack)) return -0.140f;
            return switch (id.getPath()) {
                // Example item-specific overrides (uncomment only if needed):
                // case "chest" -> 0.155f;
                // case "grass_block" -> 0.155f;
                // case "oak_fence_gate" -> 0.155f;
                default -> thirdPersonHorizontalOffset(shape(stack));
            };
        }
        return thirdPersonHorizontalOffset(shape(stack));
    }

    /** Normal vanilla doors only. Excludes trapdoors and fence gates. */
    public static boolean isVanillaDoor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && "minecraft".equals(id.getNamespace())
                && id.getPath().endsWith("_door");
    }

    /**
     * Per-item yaw corrections for the user's two-handed first-person pose.
     * The current model packs may already have their own display transforms;
     * this rotation is additional to those transforms and only applies in F1.
     */
    public static float firstPersonExtraYawDegrees(ItemStack stack) {
        if (stack.isEmpty()) return 0.0f;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !"minecraft".equals(id.getNamespace())) return 0.0f;
        String name = id.getPath();
        // All six vanilla minecart types, including the plain empty cart.
        if (name.equals("minecart") || name.endsWith("_minecart")) return 180.0f;
        // Building stairs, not ladders.
        if (name.endsWith("_stairs")) return 180.0f;
        if (name.equals("crafting_table")) return 180.0f;
        if (name.equals("lectern")) return 90.0f; // 180 degrees from the previous -90-degree orientation
        return 0.0f;
    }

    public static float rollDegrees(Shape profile) {
        return profile == Shape.BELL ? 17.0f : 0.0f;
    }
}
