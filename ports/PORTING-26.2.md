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

The full ModularUI build still fails. After the September 19 pull (`172940e31`), adding the missing `SchemaGeometry` exposed 277 method-level/API errors. Subsequent passes reduced this to 213, then 160 reported errors. This count is not a completion percentage and does not include a successful GregTech compile or runtime verification.

The pulled structure renderer also references files absent from this checkout: `SchemaRenderState`, `SchemaCameraTransform`, `SchemaPreviewRenderer`, and the configured `PreviewLightmapMixin`. Check whether these were left untracked in the other checkout before reimplementing them.

The isolated check now also compiles `SchemaGeometry`, `RadialMask` and `GuiPoseTransforms`. Ten checks pass in total, including vertex attribute/offset snapshots, attribute reset, radial coverage at eighth-turn boundaries in both directions on non-square rectangles, and rotated/scaled/transformed GUI pose snapshots and composition.

## Latest screen/input migration pass

- Screen wrappers use the extraction lifecycle. Embedded screens snapshot their 2D transform before resetting the pose and restore pose/tint in a finally block.
- Container rendering delegates carried items to Minecraft's current renderer. Obsolete drag/snapback field accessors were removed; quick-craft previews and custom ModularUI slot amounts remain. Item overlays use copied stacks rather than temporarily mutating live inventory stacks.
- Slot lookup mixin targets the exact `getHoveredSlot(double, double)` method. Resizable container dimensions use mutable accessors for the newly final fields.
- Mouse forwarding preserves event modifiers and double-click state. Text entry carries integer Unicode code points through ModularUI and the two GregTech text-editor overrides. Clipboard shortcuts use the native key-event predicates.
- Screen close tracking now targets `Gui.setScreen`, while the Minecraft timer hook uses `advanceGameTime`. Raw GL stencil enabling was removed; backgrounds/blur are extracted before stencil masks.
- The latest compile reports no errors in ClientScreenHandler, screen wrappers, EmbedHandler, ItemSlot, BaseTextFieldWidget or the updated lifecycle mixins. This does not prove mixin application or in-game behavior; those checks remain required.

## Next implementation work

1. Verify the implemented deferred stencil clipping and its GUI-state mixin in game, including rotated masks, nested masks, text, items and previews. Stencil allocation now uses the main-render-target configuration event; immediate OpenGL initialization has been removed.
2. Replace structure rendering, highlights, custom projection/viewport handling, buffer uploads, and block-entity rendering with 26.2 rendering APIs. Preserve fluids, filtering, lighting, ray tracing, and resource disposal.
3. Finish the graphics-context / widget / screen / input migration, tooltip extraction, reload hooks and mixin targets; then resolve the remaining ModularUI compilation errors.
4. Compile GregTech and migrate its remaining capabilities, item components, serialization, recipes, networking, rendering, and mixins. Audit integration dependencies individually; do not silently remove functionality to force a build.
5. Update resource/data formats, run tests and data generation, launch client and dedicated server, and exercise GUIs, recipes, machines, multiplayer synchronization, and save/reload before producing a release jar.

No successful full build or in-game validation has occurred yet.
