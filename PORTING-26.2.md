# Minecraft 26.2 port status

This is an unfinished source port, not a playable release. Changes are AI-assisted with OpenAI Codex; review them before contribution (see AI_POLICY.md).

## Target

Minecraft 26.2, NeoForge 26.2.0.88, Java 25. ModularUI is included from `ports/ModularUI`.

## Latest verification — 2026-09-20

- Full ModularUI diagnostic compilation **passes** with the user-approved unofficial EMI 26.2 alpha. Its Gradle test task also passes: **30 tests, zero failures/errors/skips**. This is not an in-game compatibility test. Diagnostic mode invokes javac directly to avoid Gradle's constant-analysis crash on missing integration types.
- Fifteen isolated rendering checks pass: six GUI geometry checks, four structure/radial geometry checks, two GUI pose checks, and three projection checks. These compile against the cached Minecraft/NeoForge artifacts, not replacement API stubs.
- The changed GUI context, widget tree, screen adapters, tooltips, item slots, and input handlers have no errors in the latest compiler output. This does not establish runtime correctness.
- GregTech's latest diagnostic compilation reports 10,798 errors (output capped at 10,000), including cascading errors. No errors are reported for `ResearchData`, `ResearchCondition`, `RecipeCondition`, `RecipeConditionType`, `GTRecipeSerializer`, `RecipeContentCodec`, `IContentSerializer`, or `SerializerBlockState` in this pass; other condition implementations and ingredient serializers remain unfinished. The isolated recipe suite passes **15 tests** (seven crafting/holder tests, four research payload tests, four shared codec transport tests). This is not a successful machine-recipe or full GT build. Client/server startup, data generation, resource reloads, and gameplay validation have not passed. No release jar is available; error counts are not a completion percentage.

## Implemented in this pass

- Migrated ResearchData/ResearchEntry JSON helpers to explicit registry lookup context and network helpers to RegistryFriendlyByteBuf/StreamCodec, preserving the `researchId`/`dataItem` fields. Added explicit constructors/accessors and owned mutable entry lists. Four tests cover component values/counts/IDs, exact binary component preservation, empty/mutable lists, and malformed entries. Vanilla JSON/NBT conversion can narrow numeric tag widths; JSON tests verify values and stable re-encoding, while binary tests require exact stack/component equality. External callers of the standalone JSON helpers must now supply a registry provider; network helpers require registry-aware buffers.
- Migrated all 17 built-in/optional recipe-condition codec declarations and their registration type to MapCodec, preserving flattened fields. Updated shared condition JSON result handling and bounded NBT decoding; network decoding/encoding uses the buffer's registry context when available, retaining the legacy built-in fallback for plain buffers. Full condition registry/runtime behavior is not yet tested.
- Extracted codec transport from IContentSerializer into RecipeContentCodec so it can be verified without loading GT machine/UI classes. Preserved UTF-JSON network framing and existing specialized numeric/item/fluid overrides; registry-aware buffers now supply their lookup context, and explicit registry-provider JSON/NBT overloads are available. Four tests pass against actual game codecs, including registry-bound holder round trips, absent context, unknown IDs and malformed values. Migrated block-state network IDs to ByteBufCodecs.idMapper and made JSON encoding failures explicit instead of silently producing null. Custom item/fluid ingredient integration remains unfinished.
- Replaced GTRecipeSerializer's obsolete interface implementation with registered RecipeSerializer records backed by MapCodec/StreamCodec, keeping explicit static helpers for custom GT synchronization and KubeJS callers. Per-machine serializer factories bind the machine type without colliding with Minecraft's outer `type` dispatch; the standalone GT codec retains its existing `type` field. Generic `gtceu:machine` JSON uses `recipeType` for the machine type. Updated recipe builder and registration callers, network identifier methods and registry lookup; replaced removed Tuple with a capability/content record. Corrected chance-logic JSON lookup to use the capability key rather than the logic value, preserving bare GT identifiers and accepting namespaced identifiers.
- Migrated GTRecipe's vanilla-facing contract to RecipeInput, explicit non-placeable/special machine-recipe behavior, and typed serializer lookup. Added RecipeIdAware/RecipeHolderMixin so codec-loaded recipes receive their holder's identifier. Seven isolated recipe tests now pass, including a real holder-mixin application test using a small recipe fixture. These tests do **not** validate GT's complete machine serializer, capability payloads, research registration, recipe-manager reload, KubeJS integration, or network synchronization; those dependencies remain unfinished.
- Reviewed the updated checkout at `98e99331f` without changing branches. Preserved the registration/fluid/NBT changes and upstream multiblock predicate refactor. Re-ran the five isolated NBT checks successfully.
- Migrated strict shaped recipes to 26.2's recipe interfaces, MapCodec/StreamCodec serializer, item stack templates, optional ingredients and recipe displays. Retained untrimmed patterns, disabled mirroring, and preserved physical grid dimensions through `CraftingInputMixin`. Added `PlaceRecipeHelperMixin` so recipe-book placement uses the strict pattern's dimensions. Six isolated NeoForge tests pass, including actual application of both mixins, padded patterns, exact grid size, extra-item rejection, independent results, JSON/network round trips and recipe-book placement. Tests use minimal component fixtures for four vanilla items, not a complete server data-pack reload. Full GT recipe registration/data generation/gameplay remain unverified.
- Separated the unofficial EMI Curse Maven file ID (`8616659`) from its actual mod version (`1.1.24-b60b8f8+26.2+neoforge`) in both metadata generators. The former must not be used as the runtime minimum version. Normal ModularUI tests still pass after restoring test-only generated metadata.
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
./scripts/check-port-recipes.ps1 -JavaHome $env:JAVA_HOME
./scripts/check-port-nbt-predicates.ps1 -JavaHome $env:JAVA_HOME
```

The isolated checks require generated Minecraft artifacts and cached JUnit dependencies. On this machine, parallel initial decompilation exhausted Windows commit memory, so use one worker.

Current local logs: `build/modularui-26.2-compile.log`, `build/modularui-26.2-tests.log`, and `build/gregtech-registration-port.log` (latest root compilation). `build/gregtech-26.2-compile.log` is the earlier root baseline. These files are ignored by Git. See `ports/PORTING-26.2.md` for historical rendering checkpoints.
