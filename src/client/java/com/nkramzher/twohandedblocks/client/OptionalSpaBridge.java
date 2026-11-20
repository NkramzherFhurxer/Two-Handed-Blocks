package com.nkramzher.twohandedblocks.client;

import com.nkramzher.twohandedblocks.BlockRules;
import com.nkramzher.twohandedblocks.TwoHandedBlocks;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Optional integration with Smooth Player Animations 2.1.0.
 *
 * SPA is NOT required to install, load or run Two-Handed Blocks.
 * A spa_graph action uses x.twohandedblocks_carry on the bendable rig.
 */
final class OptionalSpaBridge {
    private static final String MOD_ID = "spa_packs";
    private static final String VARIABLE = "twohandedblocks_carry";
    private static boolean available;
    private static Method onBentRig;
    private static Method setVariable;

    private OptionalSpaBridge() {}

    static void initialize() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            return;
        }
        try {
            Class<?> api = Class.forName("com.huaxiaosheng.spapacks.api.SpaApi");
            onBentRig = api.getMethod("onBentRig", AbstractClientPlayer.class);
            setVariable = api.getMethod("set", AbstractClientPlayer.class, String.class, float.class);
            available = true;
            TwoHandedBlocks.LOGGER.info("Optional SPA bent-rig support enabled");
        } catch (ReflectiveOperationException | LinkageError ex) {
            TwoHandedBlocks.LOGGER.warn("SPA API not available; keeping normal two-handed visuals", ex);
            return;
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!available || client.level == null) return;

            for (Player player : client.level.players()) {
                if (!(player instanceof AbstractClientPlayer animatedPlayer)) continue;
                try {
                    boolean bent = (Boolean) onBentRig.invoke(null, animatedPlayer);
                    boolean carrying = bent && BlockRules.isCarrying(player);
                    setVariable.invoke(null, animatedPlayer, VARIABLE, carrying ? 1.0f : 0.0f);
                } catch (ReflectiveOperationException | LinkageError ex) {
                    available = false;
                    TwoHandedBlocks.LOGGER.warn("SPA bridge failed; disabling optional integration", ex);
                    break;
                }
            }
        });
    }
}
