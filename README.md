# Two-Handed Blocks — prototype 0.1.0-alpha (Minecraft 26.3 / Fabric)

**Preview / source build. NOT yet verified in a real game.**

## Intended behavior

- A selected **BlockItem** in your main hand switches to a two-arm pose.
- First-person: both arms and the selected block are shown in front.
- Third-person: both arms face inward; the held block is rendered at chest level.
- The item in the offhand is kept in inventory, visually hidden, and **active offhand use** is cancelled on both client/server (block/item/entity interaction).
- Selecting food, tools, weapons, etc. restores vanilla animations.
- Regular block placement remains unchanged. Cannot pick up placed containers or mobs.

## Known caveats

- **Compilation and runtime not verified**. Inspect the GitHub Actions build output first. The transforms may need positional tweaks.
- This 0.1.0-alpha does not cancel passive effects of offhand items (e.g., held totems), only active use events.
- Compatibility with **Not Enough Animations** must be tested; it also modifies PlayerModel.setupAnim.
- Modded non-BlockItem objects that look like blocks are not included.
- Works as an ordinary mod in both client and server for offhand restrictions; the animation is client-only.

## Build

1. Upload these project files to a new GitHub repository.
2. Open **Actions**, wait for `build` to finish green.
3. Download the ZIP from **Artifacts**. Install only `two-handed-blocks-0.1.0-alpha.jar` (do not install `-sources.jar` or `-dev.jar`).
4. Test Minecraft 26.3 Fabric with Fabric API and this mod first; then add Not Enough Animations.
5. Send both the error log and screenshots of both camera modes to refine the animation.

**The mod is an original implementation**. No original Carry On code is copied or packaged.
