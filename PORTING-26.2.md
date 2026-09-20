# Minecraft 26.2 port status

This is an unfinished source port, not a playable release. Changes are AI-assisted with OpenAI Codex; review them before contribution (see AI_POLICY.md).

## Target

Minecraft 26.2, NeoForge 26.2.0.88, Java 25. ModularUI is included from `ports/ModularUI`.

## Latest verification — 2026-09-20

- Full ModularUI diagnostic compilation **passes** with the user-approved unofficial EMI 26.2 alpha. Its Gradle test task also passes: **30 tests, zero failures/errors/skips**. This is not an in-game compatibility test. Diagnostic mode invokes javac directly to avoid Gradle's constant-analysis crash on missing integration types.
- Fifteen isolated rendering checks pass: six GUI geometry checks, four structure/radial geometry checks, two GUI pose checks, and three projection checks. These compile against the cached Minecraft/NeoForge artifacts, not replacement API stubs.
- The changed GUI context, widget tree, screen adapters, tooltips, item slots, and input handlers have no errors in the latest compiler output. This does not establish runtime correctness.
- GregTech's latest diagnostic compilation reports 10,884 errors (output capped at 10,000), including cascading errors, after the predicate and registration changes below. No errors are reported for `GTRegistrate`, `GTCreativeModeTabs`, or `IGTFluidBuilder` in that output; the concrete fluid builder still needs substantial migration. Error counts are not a completion percentage. Client/server startup, data generation, resource reloads, and gameplay validation have not passed. No release jar is available.

## Implemented in this pass

- Migrated shared Registrate entry types and registration callbacks to its two-type-parameter `RegistryEntry` and NeoForge `DeferredHolder` contracts, including creative tabs and Create display entry declarations. Updated fluid registry keys and optional block lookup. Preserved the custom event priorities while initializing Registrate's stored event bus before its one-shot listeners, and moved data generation to `GatherDataEvent.Client`. Checked these lifecycle contracts against the installed Registrate artifact; registration and data generation have not been exercised in game.
- Migrated numeric recipe NBT predicates to 26.2's `NumericTag.doubleValue()`. Five isolated checks pass against the actual patched Minecraft libraries: all numeric tag kinds, comparison boundaries, missing/non-numeric values, nested paths, JSON round trips and IEEE special values. The harness does not load GT or validate component-backed ingredient integration. The normal NeoForge test attempt compiled this subset but stopped before tests because its asset-download task requires an unavailable Java 21 installation; the standalone predicate harness requires only the cached Java 25/game libraries.
- Migrated GT's four custom random-value providers to the 26.2 interfaces, bounds accessors, and direct MapCodec registration through NeoForge DeferredHolder. Updated ranged item/fluid ingredient callers and recipe quantity modifiers. Two isolated regression tests pass under ModularUI's NeoForge test loader, covering all four providers' sampling/bounds/codec identities and JSON field round trips. The harness deliberately does not load the unfinished GT mod or validate registry dispatch; ingredient serialization and custom Ingredient migration remain unfinished. Existing cast-to-int semantics are preserved.
- Pinned both builds to the approved unofficial EMI development artifact `curse.maven:emi-unofficial-port-unstable-1544558:8616659` ([author's file page](https://www.curseforge.com/minecraft/mc-mods/emi-unofficial-port-unstable/files/8616659)). Updated EMI extraction and mouse-event mixin signatures, guarded EMI mixins when the optional mod is absent, and corrected slot-local mouse coordinates. In-game alpha compatibility remains unverified.
- Updated both Curios dependencies to NeoForge `16.0.0+26.2`, resolving the test loader's incompatible Minecraft requirement. Aligned the root JEI dependency with ModularUI's 26.2 build. Corrected the geometry test's packed vertex attributes and primitive-agnostic collector contract, including snapshot idempotence and invalid attribute ordering.
- Updated JEI to 30.32.0.222 and REI to 26.2.821, with Architectury 21.0.2 and Cloth Config 26.2.155. Migrated JEI clickable-ingredient defaults/crafting-station roles and REI drawing/input/display contracts.
- Migrated test block/entity registration and item capabilities. The test item's inventory now persists in its container component; the test machine exposes a transactional capability backed by the same lists as its UI.
- Projection utilities now require an explicit projection/view matrix and depth. Removed unused implicit framebuffer-depth/global-viewport methods; callers outside this repository must migrate. Perspective projection now performs homogeneous division, covered by round-trip and viewport-offset tests.
- Updated fluid capability lookups through NeoForge's provided `FluidUtil` compatibility adapter, preserving the existing legacy tank interface while using transactional item capabilities underneath. Fluid comparisons now include components.
- Migrated recipe-screen background/foreground extraction to ordered GUI strata with exception-safe pose/tint restoration. Removed obsolete immediate color-mask calls from fluid hover drawing.
- Updated test block-entity inventory persistence to `ValueInput`/`ValueOutput`, loader environment access, registry tag lookup, codec dispatch construction, and recipe timing access. These files compile; runtime behavior remains unverified.
- Migrated crafting recipe lookup, assembly, recipe-book checks and stacked contents to 26.2. Corrected offset inventory writes and positioned recipe remainder placement; crafting-player context is cleared even if a recipe throws.
- Completed preview-world sea-level/border methods and aligned preview submissions with the current geometry snapshot API. These files report no compilation errors; in-game preview and crafting behavior remain unverified.
- Updated text selection/cursor extraction to use the deferred pipeline and 2D pose scaling.
- Updated GUI transforms to the two-dimensional extraction pose stack, with an explicit conversion for widget coordinates and a rotation/scale/picking regression check.
- Migrated tooltip text/images, panel layers, embedded screens, overlays, and item decoration extraction.
- Updated screen lookup/open/close, modifier keys, clipboard shortcuts, mouse event forwarding, and double-click state.
- Preserved full Unicode code points through character input, including the GregTech text/code editor callers.
- Replaced obsolete container drawing fields with vanilla's current carried-item renderer; updated the hovered-slot mixin target and mutable container-size accessors.

The previous rendering pass added deferred primitive/texture states, stencil clipping, entity and structure picture-in-picture rendering, fluid/block geometry extraction, preview lighting, and selection overlays. These still need in-game validation.

## Remaining work

- Validate the unofficial EMI alpha in game, including recipe rendering, clicks, dragging, scrolling, and mixin application. Development compilation preserves the integration but does not establish release readiness.
- Audit the remaining optional/runtime dependencies and integration mixins. JEI/REI now compile against 26.2 builds; other ModularUI dependencies still target 1.21.1, and the root catalog retains 1.20.1 dependencies.
- Compile the full GregTech project, finish its API/data/resource migration, and audit all mixins and access transformers against actual target members.
- Run tests and data generation, build artifacts, launch client and dedicated server, and validate GUI interactions, machines, recipes, synchronization, and save/reload.

## Reproduce

Use a Java 25 installation for `JAVA_HOME` and run from the repository root:

```powershell
./gradlew.bat :ModularUI:compileJava -PportDiagnostics --no-parallel --max-workers=1 --console=plain
./gradlew.bat -p ports/ModularUI test -PportDiagnostics --max-workers=1 --console=plain
./ports/ModularUI/scripts/check-rendering.ps1 -JavaHome $env:JAVA_HOME
./scripts/check-port-valueproviders.ps1 -JavaHome $env:JAVA_HOME
./scripts/check-port-nbt-predicates.ps1 -JavaHome $env:JAVA_HOME
```

The isolated checks require generated Minecraft artifacts and cached JUnit dependencies. On this machine, parallel initial decompilation exhausted Windows commit memory, so use one worker.

Current local logs: `build/modularui-26.2-compile.log`, `build/modularui-26.2-tests.log`, and `build/gregtech-registration-port.log` (latest root compilation). `build/gregtech-26.2-compile.log` is the earlier root baseline. These files are ignored by Git. See `ports/PORTING-26.2.md` for historical rendering checkpoints.
