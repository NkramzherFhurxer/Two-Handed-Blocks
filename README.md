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
