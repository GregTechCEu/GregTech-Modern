# Minecraft 26.2 port status

This is an unfinished source port, not a playable release. Changes are AI-assisted with OpenAI Codex; review them before contribution (see AI_POLICY.md).

## Target

Minecraft 26.2, NeoForge 26.2.0.88, Java 25. ModularUI is included from `ports/ModularUI`.

## Latest verification — 2026-09-20

- Full ModularUI diagnostic compilation reports **4 errors**, down from 39 at the start of the latest pass. All four reference the incompatible EMI 1.21.1 API (removed GuiGraphics/ResourceLocation types and recipe signatures). Diagnostic mode invokes javac directly to avoid Gradle's constant-analysis crash on missing integration types.
- Fifteen isolated rendering checks pass: six GUI geometry checks, four structure/radial geometry checks, two GUI pose checks, and three projection checks. These compile against the cached Minecraft/NeoForge artifacts, not replacement API stubs.
- The changed GUI context, widget tree, screen adapters, tooltips, item slots, and input handlers have no errors in the latest compiler output. This does not establish runtime correctness.
- GregTech's complete compilation, client/server startup, data generation, resource reloads, and gameplay validation have not passed. No release jar is available.

## Implemented in this pass

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

- Resolve EMI compatibility without silently deleting its integration. The official NeoForge Maven metadata checked on 2026-09-20 contains 61 versions and no 26.x version; the newest EMI version listed is 1.1.24+1.21.1. A compatible EMI port is required to finish this build with the integration preserved.
- Audit the remaining optional/runtime dependencies and integration mixins. JEI/REI now compile against 26.2 builds; other ModularUI dependencies still target 1.21.1, and the root catalog retains 1.20.1 dependencies.
- Compile the full GregTech project, finish its API/data/resource migration, and audit all mixins and access transformers against actual target members.
- Run tests and data generation, build artifacts, launch client and dedicated server, and validate GUI interactions, machines, recipes, synchronization, and save/reload.

## Reproduce

Use a Java 25 installation for `JAVA_HOME` and run from the repository root:

```powershell
./gradlew.bat :ModularUI:compileJava -PportDiagnostics --no-parallel --max-workers=1 --console=plain
./ports/ModularUI/scripts/check-rendering.ps1 -JavaHome $env:JAVA_HOME
```

The isolated checks require generated Minecraft artifacts and cached JUnit dependencies. On this machine, parallel initial decompilation exhausted Windows commit memory, so use one worker.

Current local logs: `build/active-port.log` and `build/scene-check.log`. These files are ignored by Git. See `ports/PORTING-26.2.md` for the rendering checkpoint.
