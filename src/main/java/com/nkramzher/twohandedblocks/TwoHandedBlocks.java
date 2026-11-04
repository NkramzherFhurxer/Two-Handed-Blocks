package com.nkramzher.twohandedblocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Core rules apply on client and server; no inventory items are moved. */
public final class TwoHandedBlocks implements ModInitializer {
    public static final String MOD_ID = "twohandedblocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Stop breaking blocks and attacking any entity while a block occupies both hands.
        // FAIL stops processing on both logical sides, preventing client-side attack packets.
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                BlockRules.isCarrying(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                BlockRules.isCarrying(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        // Only the carrying hand is allowed to use items and place blocks.
        // Main block > offhand block > normal vanilla behavior. The client
        // also selects the carrying hand before asking vanilla to use an item.
        // Passive effects such as an equipped totem remain vanilla behavior.
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                BlockRules.isInactiveHand(player, hand)
                        ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) ->
                BlockRules.isInactiveHand(player, hand)
                        ? InteractionResult.FAIL : InteractionResult.PASS);
        // Skip the block's empty-hand behavior (open chest, door, crafting table,
        // button, etc.) but keep the held BlockItem's normal placement phase.
        // Returning PASS here overrides the block-side useWithoutItem result;
        // it does NOT cancel the entire right-click chain.
        BlockEvents.USE_WITHOUT_ITEM.register((state, level, pos, player, hit) ->
                BlockRules.isCarrying(player) ? InteractionResult.PASS : null);

        // Prevent riding, trading, feeding and other entity interactions while
        // carrying a block in both hands. Unlike blocks, entities have no
        // block-placement fallback.
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                BlockRules.isCarrying(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        LOGGER.info("Two-Handed Blocks two-hand carrier priority initialized");
    }
}
