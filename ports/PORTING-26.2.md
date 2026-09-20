# Minecraft / NeoForge 26.2 port checkpoint

This is an unfinished source port, not a usable release. Code and this checkpoint were AI-assisted with OpenAI Codex. Human review and understanding are required before upstream contribution (see the root AI_POLICY.md).

## Target

- Minecraft 26.2, NeoForge 26.2.0.88, Java 25.
- ModularUI is included from `ports/ModularUI` as a Gradle composite build.
- Root integrations still include old Forge / Minecraft 1.20.1 dependencies. They are not compatible merely because the build target changed.

## Rendering work implemented

- Deferred colored geometry with captured transforms, tint, and scissor bounds; fans and strips become independent quads for GUI batching.
- Rectangle/rounded-rectangle/circle/border/shadow/graph drawing migrated to that geometry path.
- Explicit textured render states, atlas UV lookup, tiled and nine-slice textures, sprites, and scaled text.
- Entity previews use a registered picture-in-picture renderer, with captured GUI transforms and entity render states. The live entity's temporary orientation is restored after extraction.
- GUI atlas registration uses AtlasManager rather than manually owning an atlas reload listener.
- Block sprite lookup uses block-state model parts; item lookup collects all resolved item model layers.
- Structure previews use deferred block/fluid geometry, block-entity states, camera-based picking, and a scoped preview lightmap. Selection highlights are extracted quads. In-game verification remains outstanding.
- GregTech lamp overlay draw calls and the prospector map's texture upload/drawing path have been adapted. These root changes have not passed a full root compilation.

## Verification

Run from the repository root (PowerShell):

```powershell
./ports/ModularUI/scripts/check-rendering.ps1 -JavaHome 'C:/Program Files/Java/jdk-25.0.4'
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25.0.4'
./gradlew.bat -p ports/ModularUI compileJava -PportDiagnostics --offline --console=plain
```

The isolated renderer check compiles the new geometry, texture, entity-preview, sprite-lookup, and lightmap classes against the real cached 26.2 libraries. Eleven checks pass: six GUI geometry/transform checks and five structure camera/geometry/highlight checks. It requires previously generated Minecraft artifacts and cached JUnit dependencies. It is **not** a substitute for the full build or in-game tests.

The full ModularUI build still fails with **133 compiler errors** as of 2026-09-20. Diagnostic mode now invokes javac directly to avoid Gradle's constant-analysis crash. This count is not a completion percentage and excludes the full GregTech migration.

The latest pass migrated shared GUI transforms, tooltip and item extraction, screen lookup and layer ordering, mouse event forwarding/double-clicks, modifier keys and clipboard shortcuts, and full-code-point character input. Corresponding GregTech editor callers were updated. Obsolete container render accessors were replaced with current vanilla carried-item rendering.

## Next implementation work

1. Finish schema world/chunk APIs, fluid APIs, projection utilities, crafting, networking, configuration/reload hooks, and test fixtures.
2. Update recipe-viewer dependencies and integrations; audit all mixin targets and access rules. Do not remove functionality simply to force compilation.
3. Compile GregTech and finish its capabilities, components, serialization, recipes, networking, rendering, and data/resource migration.
4. Run tests and data generation, launch client/server, and exercise GUIs, recipes, machines, synchronization, resource reloads, and persistence before release.
No successful full build or in-game validation has occurred yet.
