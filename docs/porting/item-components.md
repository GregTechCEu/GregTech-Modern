# Item component migration — first slice

AI-assisted implementation and tests by OpenAI Codex, 2026-09-21. Based on `b0d5fa0d0` from `1.20.1`, working branch `codex/item-components-port`. No changes are merged or pushed to `1.20.1`.

## Representation and shared interfaces

Existing GT item fields live under vanilla `DataComponents.CUSTOM_DATA` (`minecraft:custom_data`), retaining their original names and numeric tag types. No GT component registration or custom packet is required: 26.2 `DataComponents` already registers `CustomData.CODEC` as persistent, and `DataComponentType.Builder.build()` derives the registry-aware network codec when none is specified. Both contracts were checked in the actual patched 26.2.0.88 sources. The dependency catalog confirms Minecraft 26.2, NeoForge 26.2.0.88 and Java 25; versions are unchanged.

- `ItemStackData.read(stack)` returns a detached compound. Editing it never changes the stack. Reading absent data does not attach an empty component.
- `ItemStackData.update(stack, tag -> ...)` copies current data, runs the mutation, then commits another defensive copy. Exceptions do not commit partial changes. Empty root data removes the component. Only mutate the supplied tag inside the callback; do not mutate the same stack recursively.
- `ItemStackData.updateCompound(stack, key, tag -> ...)` updates a nested compound and preserves unrelated fields. A missing or non-compound value becomes a compound, like the former get-or-create accessor.
- `ElectricItemData` owns `Charge`, `MaxCharge`, `Infinite`, `DischargeMode` and `Active` access. It contains storage semantics, not transfer policy or capability registration.
- `NightVisionItemData` reads timer defaults and merges only its fields when saving. It does not replace the whole component, which would undo an intervening battery discharge. The timer-only overload preserves the enabled field for non-helmet use.
- `IMaterialPartItem.updatePartStats(stack, mutation)` replaces the live mutable `getOrCreatePartStatsTag` API. `getPartStatsTag` now returns a detached snapshot, or null. All in-repository writers were migrated; addons must replace mutations of the old live accessor with this callback.

Do not put vanilla-owned fields such as enchantments, durability, unbreakability, custom names or block-entity payloads into this generic layer. They need their corresponding components. Block-entity save data, entity NBT, recipe payload compounds, registry tags and fluid data are not ItemStack custom data.

## Consumers migrated

| File | Behavior |
| --- | --- |
| `api/item/capability/ElectricItem.java` | Charge, maximum-charge override and infinite charge read/write. No capability architecture or transfer-algorithm changes. |
| `api/item/component/ElectricStats.java` | Discharge-mode reads and toggles; disabling removes only that field. |
| `common/item/behavior/ToggleEnergyConsumerBehavior.java` | Active-state reads and writes; explicit false remains stored. |
| `api/item/IGTTool.java` | Only direct charge/capacity readers. Their strict long-tag checks and -1 sentinel remain distinct from ElectricItem's clamped charge behavior. Other tool data is still pending. |
| `api/item/component/IMaterialPartItem.java` | All part material/damage storage, existing default-material resolution and upper-only damage clamp. Rendering and description APIs remain pending. |
| `common/item/armor/NightvisionGoggles.java` | All item-state reads and writes, timer defaults, and tooltip-state read. Effect/key/energy policy is unchanged. Removed the unused obsolete ArmorItem import. |

Charge values are not newly lower-clamped. Maximum-charge overrides still require LongTag. Infinite charge still reports the effective maximum. Discharge mode removes its key when disabled, while Infinite/Active retain explicit false. Count, copy, split and component equality use vanilla ItemStack behavior. The existing one-item restriction, simulation paths, tier checks and transfer limits in ElectricItem were left unchanged; these gameplay paths are not executed by the isolated data tests.

## Inventory and boundaries

[Baseline inventory](item-data-inventory.md) contains 351 matching lines in 110 files including related tool/part accessors. The narrower legacy tag-method search matched 306 lines. These are lexical candidates, including false positives, not compiler-error or completion counts.

- Next tool slice: `ToolHelper.getToolTag/getBehaviorsTag`, `IGTTool.get`, behavior initialization, AoE/tool-stat writers and `ItemStackMixin`. Do not change a live tag accessor to a snapshot without migrating every writer. `HideFlags`, damage, enchantments and unbreakability need dedicated component decisions, not blind custom-data replacement. Re-equip/tint code belongs to the rendering contributor.
- Remaining armor: Nano/Quark suites, advanced suits and jetpacks still use legacy tags. Their tick methods must merge only changed fields after discharging energy. `NightVisionItemData` is available where defaults and fields match.
- Recipe coordination: `ShapedEnergyTransferRecipe` still writes root `MaxCharge`/`Charge` at baseline lines 61–62 and 81–82. Use `ElectricItemData.setMaxCharge(result, capacity)` and `setCharge(result, charge)` in that subsystem's migration. No recipe architecture or code was modified here.
- Capability coordination: the modern provider must expose an `ElectricItem` bound to the actual inventory stack, not a copy. `ElectricStats` still references Forge Capability/LazyOptional/EmptyHandler. Preserve the existing IElectricItem transfer contract; this patch does not register a replacement capability.
- Serialization/network coordination: use current ItemStack codecs with registry context for full stacks. GT custom packets, machine inventory serialization and old recipe NBT transports remain other contributors' responsibility. Do not serialize just CUSTOM_DATA as a substitute for the full component-bearing stack.
- Part rendering still needs replacement of ItemColor, and its item-description call still needs the modern name API. Neither was redesigned here.
- No Lombok methods were manually replaced. Missing generated-method diagnostics elsewhere remain unresolved.

## Legacy compatibility

The 26.2 vanilla ItemStackComponentizationFix moves leftover legacy item fields into `minecraft:custom_data`; retaining GT's keys is compatible with that representation. This patch does **not** run data fixing on arbitrary old `tag` compounds or old serialized stacks. No 1.20.1 world/save upgrade was exercised. Old custom GT inventories/packets that bypass versioned vanilla migration need explicit conversion by their owners. Vanilla-owned fields moved by the fixer must be read from their modern components.

NBT persistence and networking preserve exact numeric types in the tests. JSON conversion may narrow numeric tags; a narrowed `MaxCharge` intentionally does not satisfy the legacy LongTag-only override check. JSON/SNBT/import code must retain the long type, or a separately reviewed compatibility change is needed. Reads no longer attach empty tags merely for tooltips/default checks; this prevents reads from changing stack equality.

## Verification

Clean baseline: full compilation at `b0d5fa0d0` reports **9,515 errors**. Post-change compilation reports **9,494 errors**, including cascades. These counts do not establish runtime correctness.

Exact full-compile command, with JAVA_HOME set to the installed Java 25:

```powershell
$env:JAVA_HOME='C:\Users\georg\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2'
.\gradlew.bat compileJava -PportDiagnostics --no-parallel --max-workers=1 --console=plain
```

Baseline log: original checkout `build/item-components-clean-baseline.log`. Final log: this worktree `build/item-components-compile.log`. An earlier worktree `item-components-baseline.log` overlapped initial edits and is **not** the baseline.

The final full compiler reports zero diagnostics in the three data-layer classes, ElectricItem, ToggleEnergyConsumerBehavior and NightvisionGoggles. ElectricStats has 7 remaining capability-related errors. IMaterialPartItem has 3 remaining errors (ItemColor import/type and ItemStack.getDescriptionId). IGTTool has 64 remaining errors covering its unmigrated API and item-data code. Zero diagnostics in a file does not mean the whole class hierarchy or mod runs.

Focused test command:

```powershell
$env:JAVA_TOOL_OPTIONS='-Xms16m'
.\scripts\check-port-item-components.ps1 -JavaHome 'C:\Users\georg\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2' -SkipAssets
```

The init script compiles actual GT data classes using the existing ModularUI NeoForge unit loader. Only minimal components for the real vanilla PAPER item are bound because server data packs are not loaded. There are no replacement Minecraft/NeoForge API stubs. It does not load GT's mod, test live capabilities/equipment, or exercise rendering, worlds or resources.

Normal invocation compiled the tests but stopped before execution because the asset downloader requires unavailable Java 21. `-SkipAssets` explicitly points unit-test launch metadata to an empty test asset directory using the cached 26.2 manifest's index; it does not claim to download or validate assets. A subsequent loader helper failed to allocate memory; the process-local `JAVA_TOOL_OPTIONS` above reduces its initial heap. No project toolchain versions/settings were changed.

Final result: **11 tests, zero failures/errors/skips**. Results are recorded in `ports/ModularUI/build/test-results/test/TEST-com.gregtechceu.gtceu.api.item.data.ItemStackDataPortTest.xml` and `build/item-components-tests-headless.log`. Tests cover defaults, wrong numeric types, negative/infinite charge, failed mutations, escaped tag references, unrelated data, nested updates, timer-only armor state, intervening discharge, exact NBT/network round trips and copy/split behavior. `git diff --check` passes.

Full client/server startup, capability registration, real armor tick execution and world upgrade remain unverified.
