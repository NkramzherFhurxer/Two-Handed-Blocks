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
