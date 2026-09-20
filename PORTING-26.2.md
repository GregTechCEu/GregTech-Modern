# Minecraft 26.2 port status

This is an unfinished source port, not a playable release. Changes are AI-assisted with OpenAI Codex; review them before contribution (see AI_POLICY.md).

## Target

Minecraft 26.2, NeoForge 26.2.0.88, Java 25. ModularUI is included from `ports/ModularUI`.

## Latest verification — 2026-09-20

- Full ModularUI diagnostic compilation reports **133 errors**. Earlier declaration-only counts understated the remaining work; diagnostic mode now invokes javac directly to avoid Gradle's constant-analysis crash on missing integration types.
- Eleven isolated rendering checks pass: six GUI geometry/transform checks and five structure geometry/camera/highlight checks. These compile against the cached Minecraft/NeoForge artifacts, not replacement API stubs.
- The changed GUI context, widget tree, screen adapters, tooltips, item slots, and input handlers have no errors in the latest compiler output. This does not establish runtime correctness.
- GregTech's complete compilation, client/server startup, data generation, resource reloads, and gameplay validation have not passed. No release jar is available.

## Implemented in this pass

- Updated GUI transforms to the two-dimensional extraction pose stack, with an explicit conversion for widget coordinates and a rotation/scale/picking regression check.
- Migrated tooltip text/images, panel layers, embedded screens, overlays, and item decoration extraction.
- Updated screen lookup/open/close, modifier keys, clipboard shortcuts, mouse event forwarding, and double-click state.
- Preserved full Unicode code points through character input, including the GregTech text/code editor callers.
- Replaced obsolete container drawing fields with vanilla's current carried-item renderer; updated the hovered-slot mixin target and mutable container-size accessors.

The previous rendering pass added deferred primitive/texture states, stencil clipping, entity and structure picture-in-picture rendering, fluid/block geometry extraction, preview lighting, and selection overlays. These still need in-game validation.

## Remaining work

- Finish the schema world/chunk APIs, obsolete projection utilities, fluid rendering and transfer APIs, crafting and networking, configuration/reload changes, and test fixtures.
- Migrate recipe-viewer integrations and dependencies. The catalogs still contain older Minecraft versions, including ModularUI 1.21.1 and root 1.20.1 dependencies.
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
