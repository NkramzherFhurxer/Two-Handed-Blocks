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


## Alpha 4 Hotfix 2 — visual presets and ladder exception

- Ladder (`minecraft:ladder`) is now one-handed; regular building stairs remain two-handed.
- First-person arms are raised slightly and turned toward the object's sides. This is an experimental pose adjustment.
- Added one central client-only helper, `CarriedModelProfile`, to orient different item-model families.
- Fences and walls are yawed relative to the cube orientation; fence gates deliberately unchanged.
- Beds receive a separate yaw preset, heavy core restores earlier yaw, and bells receive a small roll correction.
- Brewing stand remains unchanged: showing its placed 3D block model requires a separate rendering strategy.
- All attack, use, offhand and previous one-handed exceptions preserved.
- Uses only the existing Minecraft/Fabric dependencies. This ZIP is SOURCE, not a compiled JAR.
- NOTE: orientation choices are experimental and require first-person / third-person screenshots and GitHub Actions build verification. No Java 25 compiler is available in this environment.


## Alpha 4 — Hotfix 3: tercera persona y bloques administrativos

- Se excluyen `minecraft:light` (todos los niveles de iluminación), `minecraft:barrier` y `minecraft:structure_void` de la postura de dos manos y de sus restricciones.
- En tercera persona, la mayoría de los modelos dejan de recibir el giro adicional de -90° X / 135° Y. En su lugar se aplica una compensación de -45° Y y -75° X para cancelar la rotación habitual de los modelos de objeto sostenidos en la mano.
- **Primera persona sin cambios:** mantiene las orientaciones aprobadas de cofres, vallas, muros y demás.
- **Campana sin cambios:** conservará el perfil visual anterior hasta que se revise por separado.
- Conserva clasificación, restricciones e inventario de la versión anterior.
- Esta compensación está diseñada para los modelos normales de los bloques de Minecraft; algunos modelos especiales y resource packs pueden necesitar calibración adicional.
- Pendiente de compilar y verificar visualmente en Minecraft 26.3.


## Alpha 4 — Hotfix 4: ajuste F5 (experimental)

- **Solo tercera persona**: se agrega un giro de 180° alrededor de Z para intentar corregir los bloques boca abajo observados en Hotfix 3.
- El objeto completo se mueve `-0.15` unidades en el eje X local para compensar su desplazamiento común hacia el brazo izquierdo del personaje.
- Vallas y muros reciben `+90°` de giro horizontal adicional para presentar las conexiones de frente, tomando las puertas de valla como referencia.
- Las puertas de valla conservan su perfil de orientación horizontal; no reciben el giro extra de vallas/muros.
- La campana conserva su perfil de rotación independiente, que se revisará después.
- Sin cambios en primera persona, clasificación de objetos, ataques, interacciones, inventario ni exclusiones de `light`, `barrier` y `structure_void`.
- **Visualmente experimental**: verificar con cofres, césped, vallas, muros y puerta de valla. No se pudo compilar ni probar con el juego en este entorno.


## Alpha 4 — Hotfix 5: centering of carried items in F5

- Keep all the third-person orientation / rotations from Hotfix 4 (they were validated upright).
- Add shape-specific third-person X position to compensate for differently-pivoted item models.
- Full blocks, chests, beds, heavy core and fence gates use +0.18 local X (previously -0.15), targeting centered placement between the hands.
- Fences and walls remain at -0.15 local X; user-confirmed centered in Hotfix 4.
- Bell retains its previous offset and visual preset pending a separate fix.
- First-person rendering, arm animations, attacks, placement and interaction rules, and item classifications are unchanged.
- Light, barrier and structure void remain excluded from carrying logic.
- Experimental visual calibration: build and validate with front-facing F5 screenshots before treating offsets as final.

**Note:** Java 25 compilation and in-game runtime verification were not available in this environment.


## Alpha 4 — Hotfix 6: microajuste del centrado F5

- Solo cambia el desplazamiento X local de los modelos de dos manos de perfil DEFAULT/BED/HEAVY_CORE: +0.18 a **+0.20** (0.02 unidades hacia el lado que ya corregimos en Hotfix 5).
- Incluye cofres, bloques completos y puertas de valla dentro del perfil DEFAULT.
- Vallas y muros permanecen en **-0.15** (centrado ya confirmado). La campana permanece sin cambios.
- Rotaciones, primera persona, escala, brazos, clasificación, ataques e interacciones: sin cambios.
- Se requiere compilar y verificar visualmente en el juego; es un microajuste experimental.


## Alpha 4 Hotfix 7 (third-person fine alignment + compile fix)

- Fixes the `ItemStack cannot be converted to Shape` Java compilation error by
  adding an `ItemStack` overload in `CarriedModelProfile` and calling it from
  `PlayerItemInHandLayerMixin`.
- Horizontal offsets in third person: standard blocks + fence gates `0.19f`
  (was `0.20f`), bell `-0.16f` (was `-0.15f`).
- Fences and walls stay `-0.15f` exactly; they were correctly centered already.
- Adds commented-out examples for future per-item offsets (`chest`,
  `grass_block`, `oak_fence_gate`). Uncomment them individually as needed.
- No changes to first-person rendering, orientation, interaction rules, or
  one-handed exceptions.
- Static validation done; full Gradle build with Java 25 and in-game test
  remain to be checked in GitHub Actions.

## Alpha 4 Hotfix 8 — offhand carrying priority (experimental)

- Priority: main-hand two-handed block > offhand two-handed block > normal one-handed gameplay.
- An offhand block activates the two-arm pose and the same restrictions on attacks, breaking,
  sprint-swimming and steering boats, minecarts, horses and nautiluses.
- Only the selected carrying hand can place/use its item. The other hand stays in inventory
  but is visually suppressed in first- and third-person.
- On right-click, the client skips the inactive hand rather than letting the main-hand item
  (for example, a torch) consume the action before the offhand block.
- If both hands hold two-handed blocks, the main-hand block is displayed/used.
- Single-hand gameplay retains vanilla order and behavior.
- No changes to texture packs or per-block display transforms.
- Passive offhand effects such as a totem are not disabled.

**Test in game:** try (1) sword + offhand stone, (2) torch + offhand chest,
(3) main-hand stone + offhand diamond block, (4) swapping hands with F,
(5) placing against chests/doors, (6) F1/F5 with left-handed settings,
(7) attacks/swimming/riding with offhand-only carried blocks.
The new right-click interception is experimental and needs real gameplay testing.

## Alpha 4 Hotfix 9 — offhand bell alignment (visual test)

- First-person: shift the offhand bell slightly right and counter-rotate the tilt.
- Third-person: mirror its bell-only torso offset and quarter-turn the sideways item pose.
- Main-hand bell and all other block positions/transforms unchanged.
- Block-Accurate v23 remains unchanged. No resource pack edits.
- This is a first visual calibration. Compare F1/F5 offhand screenshots in-game
  before treating the angles as final.

## Alpha 4 Hotfix 10 — shared model orientation for either carrying hand

- When the offhand carries the selected two-handed block, its baked item model
  uses the same first-person and third-person display context as the main hand.
- Right-handed players use the RIGHT-hand item context as the reference.
  Left-handed players use their LEFT-hand item context.
- Removes the experimental bell-only offhand F1/F5 offsets from Hotfix 9.
  The existing main-hand offsets, yaw/roll, and animations are unchanged.
- Affects two-handed offhand blocks only, not ordinary one-handed items,
  GUI previews, dropped items, or placed blocks.
- Does not modify the resource pack; Block-Accurate v25 stays intact.
- This normalizes model transforms, not inventory/interaction priority, which
  continues to follow the previously established main > offhand rule.

**Test:** switch the same bed, bell, chest, boat, minecart and ordinary block
between primary and offhand while observing F1/F5 (including player left-handed
setting). Their model orientation/position should match; no right-click,
mining, swimming, or mounting regressions.

## Alpha 4 Hotfix 11 — F1 cached offhand render state

- Hotfix 10 worked in F5 but the original first-person offhand models did not
  reliably match the main-hand orientation.
- First-person rendering may use a cached/copy of the player's offhand item.
  The former object-identity check was too strict. F1 now matches both item
  and components (not object identity) before applying the main-hand model
  display context.
- The successful F5 logic, model transforms, poses, handling mechanics,
  and resource packs remain untouched.
- Test F1 offhand bell, bed, chest, boat, minecart, etc. beside the same item
  held in the primary hand; verify F5 is unchanged. This is a candidate fix,
  pending screenshots from actual gameplay.

## Alpha 4 Hotfix 12 — direct first-person state correction (experimental)

**Hotfix 11 in-game result:** no change in F1; F5 continued to work.

- First-person and third-person build their render states along different paths.
  Our old `ItemModelResolver.updateForLiving` injection did not normalize
  the first-person model in actual gameplay.
- Now directly rebuild `state.offHandRenderState` at the start of
  `FirstPersonHandsAndItemsRenderer.submitHandsWithItems`, using the cached
  F1 item and the player's primary-hand `ItemDisplayContext`.
- The former shared resolver mixin was narrowed to the unchanged, confirmed
  working F5 path only. F1 doesn't depend on its ItemStack identity any more.
- The exact same `FirstPersonBlocksMixin` arm position, model rotations and
  scale are reused once the F1 render state is normalized.
- No changes to texturepack files, item priority, block rules, gameplay controls
  or the validated third-person pose.

**Verify in-game** by putting a bell in the main hand, taking an F1 screenshot,
then swapping it to the offhand and taking another. Repeat with a bed and a
minecart. F5 must remain unchanged. A successful compile does not guarantee
visual equivalence.

## Alpha 4 Hotfix 13 — dried ghast, copper bars and F1 front faces

- Keep `minecraft:dried_ghast` in **one hand**, without any two-hand posture
  or restrictions.
- All 8 vanilla copper bars variants (four oxidation states, waxed/unwaxed)
  are also **one-handed**; iron bars remain unchanged.
- Turn only `minecraft:crafter` and `minecraft:observer` horizontally by
  180° in first-person, keeping their scale, translation, pitch, roll, third-person
  alignment and all other objects unchanged.
- The corresponding Block-Accurate v26 texture pack is distributed separately:
  it adds the vanilla **decorated pot** special renderer and editable model
  display transforms, preserving ceramic decoration data.
- No other mod-side model or behavior changes from working Hotfix 12.

**Manual verification:** dried ghast, all copper bar oxidized/waxed variants,
crafter/observer in F1 and F5, decorated pot while carrying in first and third
person, and offhand priority in both views. Test the decorated pot texture pack
with no other pack overriding its files; its baseline visual transforms are
vanilla and may need further adjustment.

## Alpha 4 Hotfix 14 — decorated pot F1 grip (experimental)

- No change to the already proven two-hand priority and F1/F5 offhand matching.
- Only when carrying `minecraft:decorated_pot`, bring the arms 0.06 closer to
  the object's sides and lower their pose by 0.12 in first-person so the
  player no longer appears to grip its upper corners.
- All other blocks and vehicles retain their existing poses exactly.
- Third-person pot orientation is addressed **only** in companion resource
  pack Block-Accurate v27: special item model retained and
  `thirdperson_righthand.rotation` experimentally calibrated from
  `[0,90,0]` to `[75,45,180]`. No other model transforms were changed.
- This calibration requires actual in-game F1/F5 screenshots. If the pot is
  still tilted or the arms miss its sides, adjust only the pot parameters.
- The v26 pack and Hotfix 13 are valid previous backups.

## Alpha 4 Hotfix 15 — pot grip follow-up

- User testing of Hotfix 14: decorated pot **still gripped at its corners in F1**.
  In F5, the pot stood vertically but was upside-down.
- In F1 only, for `minecraft:decorated_pot`, hands move closer to the pot's
  sides (0.37 to 0.30), higher (-0.03 to 0.10) and angle inward slightly less
  (32° to 28°). Every other block's pose remains identical.
- F5 pot correction lives **only** in Block-Accurate v28 (separate ZIP):
  change model third-person rotation [75,45,180] to [75,45,0], removing
  the extra Z half-turn that appeared to invert the pot.
- Prior working Hotfix 12 hand precedence and orientation normalization remain
  unchanged. Both modifications are visual experiments requiring gameplay
  screenshots, not guaranteed final alignments.

## Alpha 4 Hotfix 16 — raise the decorated-pot F1 grip noticeably

- Hotfix 15 feedback: the hands **still** held the pot at its bottom corners in F1.
- Found why: that version set pot-only F1 arm height to `0.10f` versus
  the original `0.09f`; effectively no improvement.
- Change only the pot's F1 arm height to `0.38f`, retaining the existing
  pot-only side offset `0.30f` and yaw `28°`. This should move the arms
  upward alongside the pot, not move, rotate or scale the pot itself.
- All other object poses, F5, hand priority, combat/swim/mount restrictions,
  and resource pack Block-Accurate v28 remain untouched.
- Needs actual game screenshots: if arms are now too high, refine the
  pot-only height; **do not** modify other item profiles.

## Alpha 4 Hotfix 17 — revert pot-only arm offsets

- In-game feedback on Hotfix 16: the decorated pot appeared to make both
  forearms stretch unnaturally in F1. It was caused by moving the whole arm
  render vertically to +0.38f, not by any change to player geometry.
- Restore the stable first-person arm pose for **all** two-handed items:
  side 0.43f, vertical 0.09f, yaw 32°. Remove the separate decorated-pot
  arm pose and its unused predicate.
- The companion Block-Accurate **v29** resource pack shifts only the pot's
  first-person item-model translation to [0,2,0] (was [0,0,0]), aiming
  for a more natural grip with normal-length arms. This adjustment can be
  edited in the pack and tested with F3+T. F5 is not changed from v28.
- All other models, the working offhand priority, and movement/interaction
  restrictions remain unchanged. This is a visual hypothesis pending an
  in-game screenshot.

## Alpha 4 Hotfix 18 — Totem of Undying and occupied hands

- When the player carries any two-handed block in the main hand, a Totem of
  Undying equipped in the offhand does **not** activate upon fatal damage.
- When the player carries a two-handed block in the offhand, a totem in the
  main hand also does **not** activate.
- The inactive totem is **not consumed**; once the carried block is deselected
  or removed, the totem works normally again.
- Players with a one-handed item plus totem, or no two-handed block at all,
  retain vanilla totem protection. Mobs holding totems are unaffected.
- Implemented at the server-side `LivingEntity.checkTotemDeathProtection`
  decision point, without changing F1/F5 animations, priority, movement
  restrictions or resource-pack files.
- Only vanilla Totems of Undying in the inactive hand are suppressed; other
  modded death-protection mechanics are not globally disabled.

**Important testing:** test on a disposable survival world with cheats;
do not rely on a totem in a two-handed situation until verified. Use real
fatal damage or `/damage @s 100 minecraft:generic` (NOT `/kill`, which
bypasses totems even in vanilla). Test (1) stone main + totem off, (2)
totem main + stone off, (3) sword main + totem off, (4) stone main then
switch to sword without moving the totem, and (5) a mob holding a totem.
Verify the totem is not consumed in cases (1) and (2).

## Alpha 4 Hotfix 19 — no hand-powered climbing with two-handed blocks

- Carrying a two-handed block in **either hand** makes the player unable to
  climb anything via Minecraft's `LivingEntity.onClimbable` mechanism.
  This covers vanilla ladders, scaffolding, ordinary vines, Nether twisting
  and weeping vines, cave vines, and usable trapdoors above ladders.
- Scaffolding can still be jumped up normally even when its climbable status
  is false, so suppress `jumpFromGround` while inside or standing directly
  on scaffolding with a two-handed item.
- This applies on both client and logical server. Mobs and players not
  carrying two-handed blocks behave as in vanilla.
- Soul-sand bubble-column elevators, magma bubble columns, water currents,
  ordinary movement, jumps on solid blocks, falls and externally imposed
  forces are NOT intercepted. Do not clamp vertical motion globally.
- Existing F1/F5 block poses, offhand priorities and Hotfix 18 totem
  behavior remain unchanged. Block-Accurate v33 is untouched.

**Test in Minecraft:** hold stone (main hand), then stone (offhand) while
attempting to climb a ladder, scaffolding, vine on a wall, twisting vines,
weeping vines and cave vines; repeat with one-handed item to verify climbing
works normally. Check bubble-column upward lift with soul sand and water
in both cases, plus normal jumps on solid ground.

## Alpha 4 Hotfix 20 — integrated models and NEA coexistence (experimental)

- The approved Block-Accurate v33 pack can now be embedded in the mod JAR
  under `resourcepacks/twohanded_visuals/` with the Fabric resource-loader
  v1 `ResourceLoader.registerBuiltinPack(... ALWAYS_ENABLED)` API.
- This built-in pack remains enabled whenever the mod runs. **Being
  required/always enabled does not guarantee higher priority over all user
  packs**; resource ordering and server-provided packs may override it.
  This still requires in-game verification with NewDefault+.
- The mod detects the optional `notenoughanimations` mod. Its existing
  priority-500 PlayerModelMixin applies our final arm pose only for two-handed
  blocks, leaving one-handed animations unchanged. NEA is **not embedded**:
  users must install its independent Fabric JAR (1.12.6 or newer).
- Build with an explicit ZIP to produce a fully integrated jar:
  `./gradlew build -PtwohandedPackZip=/path/to/Block-Accurate-v33.zip`
  (on Windows use `.\\gradlew build -PtwohandedPackZip="C:/path/Block-Accurate-v33.zip"`).
  Gradle extracts only `assets/**` and attribution/license files into the
  built-in pack. The original resource pack can then be disabled in Minecraft.
- **IMPORTANT:** A vanilla `./gradlew build` without that ZIP still compiles
  a code-only mod JAR with only pack metadata. The default GitHub Actions
  artifact is therefore NOT the integrated release; use the specially
  assembled integrated JAR or build locally with the explicit ZIP property.
- Do not repackage or include the Not Enough Animations JAR within this mod;
  it remains independent and retains its own license.
- The original Block-Accurate models are MIT licensed, while the adapted bell
  model/texture by toni_4819 retain CC BY-NC 4.0, as noted in the embedded
  model pack's `LICENSE.txt` and `ATRIBUCION-CAMPANA.txt`.
- Before declaring NEA support confirmed, test F5 walking, swimming and riding,
  alternating carried block/offhand, map, lantern and boat poses. This
  compatibility is source-level/collision checked, **not yet in-game verified**.

## Alpha 4 Hotfix 21 — experimental protected F1/F5 models

The client now selects namespaced item models `twohandedblocks:held/<item>`
**only during rendering** of supported carried items in first or third person.
It uses a disposable `ItemStack.copy()` with `DataComponents.ITEM_MODEL` set to
the private model ID. The player's actual inventory item is never changed.
Existing two-arm animations and hand priority remain intact.

Protection scope:
- Approved Block-Accurate v33 models: bells, beds, boats, minecarts, doors,
  decorated pots and other bundled 3D models.
- Other items fall back to the pre-existing renderer until protected models
  have been explicitly generated and verified.
- Texture identifiers remain vanilla where possible, allowing texture-only
  resource packs to recolor the protected geometry.
- This is NOT absolute protection from arbitrary mods or packs intentionally
  modifying the `twohandedblocks` namespace. Beds still refer to vanilla
  `minecraft:block/<bed>_head/foot` geometry and may require further
  namespacing if another pack overrides those blocks.

IMPORTANT: The GitHub source branch does not yet carry the full Block-Accurate
binary resource pack. To test, use the specially **integrated Hotfix 21 JAR**
which contains both the original v33 models and new protected-namespace
definitions. A bare GitHub Actions build from this experimental branch is NOT
ready for play until those resources have been bundled.

Test with a separate, known conflicting resource pack and compare F1/F5:
- bell, decorated pot, bed, minecart and boat;
- main and offhand;
- original textures versus other compatible texture replacements;
- inventory, world placement and one-handed objects unchanged.
Do not merge this experiment into main until confirmed.
