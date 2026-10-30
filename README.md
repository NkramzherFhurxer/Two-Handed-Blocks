# Two-Handed Blocks 0.2.0 Alpha 2 (experimental)

Based on working 0.2.0 Alpha 1.

## Changes
- Cancels block-breaking and entity attacks (retained from Alpha 1).
- Prevents entity interactions (riding, trading, feeding) when the main hand carries a BlockItem.
- Overrides the vanilla block `useWithoutItem` action with `PASS` when carrying, aiming to bypass opening chests, crafting tables, doors, buttons, etc. while allowing subsequent BlockItem placement.
- Offhand interaction blocking from Alpha 1 retained.

## Important limitations
- Experimental: cannot guarantee the right-click chain behaves as intended without in-game testing. The block-specific `useItemOn` stage may still intercept certain interactions.
- Offhand-held blocks do not yet trigger the mode.
- Swimming, riding controls, pose adjustments, passive offhand effects not implemented.
- Needs GitHub Actions compilation; never put this source ZIP inside Minecraft mods.

## Testing
1. Confirm placing a block onto dirt, chest, crafting table and door.
2. Confirm chest/table UI and door/button actions do not trigger.
3. Try villager trade and boarding an empty boat while holding a block.
4. Switch to a pickaxe and confirm everything is interactive again.
5. Verify attacks and mining remain blocked when holding a block.

If placement fails on an interactive block, report the precise target and what occurs.


## Alpha 3 — small placeable blocks (initial list)

Small items such as torches, levers, buttons, pressure plates, carpets, rails, candles, signs, flowers, saplings and pillows no longer trigger the two-handed pose **or** its interaction restrictions. The list is defined in `BlockRules.java` and can be expanded after testing.

**Always two-handed:** cactus, all fences and fence gates, walls, full blocks, chests and barrels. Most modded blocks default to two-handed pending specific support.

This is an **experimental classification**, not a complete catalog of all Minecraft 26.3 placeable items. Keep reporting exceptional cases. The offhand feature and swimming/mount controls are not yet implemented.


## Alpha 3 Hotfix 2 — one-hand exceptions
- One hand: copper chains (all oxidation/waxed variants), iron chain, copper lanterns (all variants), pitcher plant/pod, sulfur spike, pale hanging moss, mangrove propagule, short dry grass, sunflower, tall grass, large fern, lilac, torchflower seeds, end rod, string/tripwire, and all four amethyst buds/clusters.
- Removed the explicit two-handed overrides that previously captured pitcher plant, sunflower, tall grass, large fern and lilac.
- Two hands remain mandatory for cactus, fences/fence gates, walls and bell.
- Note: common string is a non-BlockItem and already remains one-handed; tripwire is included defensively.
- No changes to combat, placing, interaction code or animation code.
- Needs compilation and in-game verification.

## Alpha 3 Hotfix 3
- `minecraft:iron_chain` is one-handed (older `chain` retained for compatibility).
- `minecraft:string` is always one-handed; this exception is checked before the BlockItem classification.
- All other classification and interaction restrictions are unchanged.


## Alpha 3 — Hotfix 4

- One-handed: all vanilla trapdoors (wood, iron, copper variants, poplar).
- One-handed: all vanilla shelves (including poplar).
- Retains the Hotfix 3 fixes for `minecraft:iron_chain` and `minecraft:string`.
- Preserves existing two-handed cactus, fences, fence gates, walls, and other block rules.
- Build and in-game verification are still pending.


## Alpha 4 — visual tuning (experimental)
- First-person block size reduced (1.25 to 0.90), shifted away from the camera; arms are offset outward and angled toward its sides.
- Third-person block enlarged slightly (1.25 to 1.33) with centering adjusted.
- Third-person arm angles tuned for a side-grip pose.
- No changes to item classifications, combat restrictions, or interactions.
- Requires GitHub Actions compilation and in-game evaluation in both views, including left-handed player settings.


## Alpha 4 Hotfix 1 — item orientation (experimental)
- In both first and third person, adjusted the carrying block's Y rotation from 180 to 135 degrees to counteract vanilla block item-display yaw (~45 degrees).
- Preserved existing arm animations, block scale, offset, attack/interaction rules, and one-hand exceptions.
- This compensating angle is a visual hypothesis based on screenshots and standard display transforms; it must be tested in-game with chest, barrel, and a normal cube.
- Requires compilation in GitHub Actions (Java 25).
