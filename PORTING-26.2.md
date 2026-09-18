# Minecraft 26.2 port status

This checkout is an incomplete port. It does not yet produce a working mod.

## Target and build

- Minecraft 26.2, NeoForge 26.2.0.88, Java 25.
- The root build provisions Java 25 through the same Foojay resolver used by ModularUI.
- The vendored UI library is in `ports/ModularUI`.
- On this machine, the first parallel build exhausted Windows commit memory. Retrying with
  `--no-parallel --max-workers=1` completed Minecraft decompilation and recompilation for both projects.

```text
gradlew compileJava -PportDiagnostics --no-parallel --max-workers=1 --console=plain
gradlew :ModularUI:compileJava -PportDiagnostics --no-parallel --max-workers=1 --console=plain
```

## Changes checked on 2026-09-19

- Updated moved RenderType, ModelData, and OpenGL helper imports in both projects.
- Replaced ModularUI's ClickType references with ContainerInput, whose enum values and
  container click signature were checked against the generated Minecraft sources.
- Replaced FastColor.ARGB32 with ARGB in GradientUtil.
- Migrated ModularUI reload events to AddServerReloadListenersEvent and
  AddClientReloadListenersEvent, including identifiers for client listeners.
- Updated the server-resource constructor mixin to read NeoForge's registryAccess field
  at constructor return, without binding to the obsolete constructor arguments or TagManager field.
- Replaced TextureAtlasHolder with a TextureAtlas owned by TextureManager and an asynchronous
  reload listener. Stitching and mip preparation complete before the reload barrier; upload runs
  on the reload executor. The custom atlas definition remains in use.
- Updated 14 access-transformer rules against actual 26.2 source signatures, including the
  LootItem constructor's Holder and List parameters. These rules no longer appear in the
  source transformer's missing-target report.

## Validation limits and remaining work

The initial ModularUI compiler pass reported 73 errors. The latest pass reports 35
declaration errors. This is not the total remaining port workload: javac may expose more
errors after the missing types are resolved. GregTech compilation remains blocked by ModularUI.
The changed classes have not passed a complete compilation or runtime test.

The next major task is ModularUI rendering: Stencil, GuiDraw, CircularProgressDrawable,
BaseSchemaRenderer, BlockHighlight, DummyLightTexture, RenderLevel, MUIRenderTypes,
SpriteHelper, ClientScreenHandler, and RichTooltipEvent still reference removed APIs.
ShaderInstance, BufferUploader, Tesselator, MultiBufferSource, VertexBuffer, BakedModel,
LevelTimeAccess, RenderStateShard, and the old tooltip/screen events need behavioral ports.

Eight access rules still target removed members: the old no-silk-touch field, block-model
generator helpers, item-model generator field, attribute UUID fields, boat type field, and
chunk beginLayer method. Their consumers need updates before those rules can be replaced or removed.
Interface injection and all mixin targets also need a full audit.

The main dependency catalog still includes 1.20.1 integrations, and ModularUI's catalog
includes 1.21.1 integrations. They need compatible dependency versions and API migrations.
After both projects compile, run the tests, build the jars, and validate client startup,
dedicated-server startup, resource reloads, GUI interactions, recipes, and world persistence.
None of those end-to-end checks has passed yet.

Local diagnostic logs are under `build/port-compile.log`, `build/modularui-compile.log`,
and `build/access-rules-check.log`; Gradle ignores this directory.

AI disclosure: OpenAI Codex generated the changes described above and ran the diagnostic builds.
