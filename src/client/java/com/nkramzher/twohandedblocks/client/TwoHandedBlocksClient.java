package com.nkramzher.twohandedblocks.client;

import com.nkramzher.twohandedblocks.TwoHandedBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

/**
 * The customized Block-Accurate assets are a bundled, required resource pack.
 * They are stored in resourcepacks/twohanded_visuals in the mod JAR.
 *
 * ALWAYS_ENABLED prevents accidentally disabling the matching two-handed
 * item models. Resource-pack ordering is still managed by Minecraft, so a
 * conflicting higher-priority user/server pack can override these assets.
 */
public final class TwoHandedBlocksClient implements ClientModInitializer {
    private static final Identifier VISUAL_PACK_ID =
            Identifier.fromNamespaceAndPath(TwoHandedBlocks.MOD_ID, "twohanded_visuals");

    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer(TwoHandedBlocks.MOD_ID).ifPresent(container -> {
            boolean registered = ResourceLoader.registerBuiltinPack(
                    VISUAL_PACK_ID, container, PackActivationType.ALWAYS_ENABLED);
            if (registered) {
                TwoHandedBlocks.LOGGER.info(
                        "Built-in Block-Accurate v33 item models enabled; "
                        + "check resource pack order if another pack replaces these models");
            } else {
                TwoHandedBlocks.LOGGER.warn(
                        "Bundled item models not registered: missing resourcepacks/twohanded_visuals?");
            }
        });

        // Not Enough Animations uses independent third-person animation
        // transformers. Our PlayerModelMixin is intentionally priority 500:
        // for two-handed blocks it reapplies the final two-arm hold pose,
        // while one-handed gameplay remains fully owned by vanilla / NEA.
        if (FabricLoader.getInstance().isModLoaded("notenoughanimations")) {
            TwoHandedBlocks.LOGGER.info(
                    "Not Enough Animations detected: two-handed block arm pose overrides its "
                    + "general third-person pose; other NEA animations remain active");
        }
    }
}
