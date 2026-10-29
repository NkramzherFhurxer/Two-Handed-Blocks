package com.nkramzher.twohandedblocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Core rules apply on client and server; no inventory items are moved. */
public final class TwoHandedBlocks implements ModInitializer {
    public static final String MOD_ID = "twohandedblocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Cancel active off-hand interactions while the selected MAIN-hand item is a block.
        // Note: passive off-hand effects (e.g. a totem) are not cancelled in this preview.
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                hand == InteractionHand.OFF_HAND && BlockRules.isCarrying(player)
                        ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) ->
                hand == InteractionHand.OFF_HAND && BlockRules.isCarrying(player)
                        ? InteractionResult.FAIL : InteractionResult.PASS);
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                hand == InteractionHand.OFF_HAND && BlockRules.isCarrying(player)
                        ? InteractionResult.FAIL : InteractionResult.PASS);
        LOGGER.info("Two-Handed Blocks (prototype) initialized");
    }
}
