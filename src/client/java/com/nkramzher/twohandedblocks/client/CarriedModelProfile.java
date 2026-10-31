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
            default -> 0.17f;            // Fine adjustment toward screen-left; fences and walls unchanged.
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
            return switch (id.getPath()) {
                // Example item-specific overrides (uncomment only if needed):
                // case "chest" -> 0.17f;
                // case "grass_block" -> 0.17f;
                // case "oak_fence_gate" -> 0.17f;
                default -> thirdPersonHorizontalOffset(shape(stack));
            };
        }
        return thirdPersonHorizontalOffset(shape(stack));
    }

    public static float rollDegrees(Shape profile) {
        return profile == Shape.BELL ? 17.0f : 0.0f;
    }
}
