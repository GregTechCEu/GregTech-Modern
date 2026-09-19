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
- Preview lighting uses the new Lightmap API and environmental attributes; connecting it to the replacement structure renderer is still required.
- GregTech lamp overlay draw calls and the prospector map's texture upload/drawing path have been adapted. These root changes have not passed a full root compilation.

## Verification

Run from the repository root (PowerShell):

```powershell
./ports/ModularUI/scripts/check-rendering.ps1 -JavaHome 'C:/Program Files/Java/jdk-25.0.4'
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25.0.4'
./gradlew.bat -p ports/ModularUI compileJava -PportDiagnostics --offline --console=plain
```

The isolated renderer check compiles the new geometry, texture, entity-preview, sprite-lookup, and lightmap classes against the real cached 26.2 libraries. Five geometry regression checks pass. It requires previously generated Minecraft artifacts and cached JUnit dependencies. It is **not** a substitute for the full build or in-game tests.

The full ModularUI build still fails. The current first compiler barrier is missing legacy rendering types in `Stencil`, `CircularProgressDrawable`, `BaseSchemaRenderer`, and `BlockHighlight`. Once these type-resolution errors are resolved, further method-level errors may become visible. Do not interpret the current diagnostic count as the total work remaining.

## Next implementation work

1. Replace stencil clipping while retaining rotated masks and circular-progress masks, including text, items and entity previews. Axis-aligned scissors alone are insufficient. The existing stencil initialization in ModularUIClient also needs replacement.
2. Replace structure rendering, highlights, custom projection/viewport handling, buffer uploads, and block-entity rendering with 26.2 rendering APIs. Preserve fluids, filtering, lighting, ray tracing, and resource disposal.
3. Finish the graphics-context / widget / screen / input migration, tooltip extraction, reload hooks and mixin targets; then resolve the remaining ModularUI compilation errors.
4. Compile GregTech and migrate its remaining capabilities, item components, serialization, recipes, networking, rendering, and mixins. Audit integration dependencies individually; do not silently remove functionality to force a build.
5. Update resource/data formats, run tests and data generation, launch client and dedicated server, and exercise GUIs, recipes, machines, multiplayer synchronization, and save/reload before producing a release jar.

No successful full build or in-game validation has occurred yet.
