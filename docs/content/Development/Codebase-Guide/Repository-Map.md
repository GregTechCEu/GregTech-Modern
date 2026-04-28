---
title: Repository Map
---


# Repository Map

GTCEu Modern is a large NeoForge mod with most of its behavior in Java and most of its declarative content in resources
or generated resources. The quickest way to get oriented is to separate API-style extension points from built-in GTCEu
content.


## Main Source Areas

| Area | Path | Purpose |
| --- | --- | --- |
| Mod entry point | `src/main/java/com/gregtechceu/gtceu/GTCEu.java` | Owns the mod id, logger, helper id creation, config init, common init, network registration, and GameTest bootstrap hook. |
| Public and semi-public API | `src/main/java/com/gregtechceu/gtceu/api` | Reusable contracts for machines, materials, recipes, capabilities, covers, GUI, sync, pipenets, transfer, sounds, placeholders, and registries. |
| Built-in GTCEu content | `src/main/java/com/gregtechceu/gtceu/common` | The concrete blocks, items, machines, covers, recipes, fluids, commands, worldgen, data constants, and server/common listeners. |
| Client implementation | `src/main/java/com/gregtechceu/gtceu/client` | Client-only registration, model loaders, item tint/model properties, renderers, overlays, particles, and client event listeners. |
| Data generation | `src/main/java/com/gregtechceu/gtceu/data` | Registrate provider setup, tags, language, recipes, loot, model builders, data maps, and dynamic pack support. |
| Optional integrations | `src/main/java/com/gregtechceu/gtceu/integration` | Compatibility with AE2, CC: Tweaked, Create, EMI, Jade, JEI, KubeJS, maps, ModernFix, REI, and shared XEI widgets. |
| Mixins and core hooks | `src/main/java/com/gregtechceu/gtceu/core` | Mixin plugin, runtime hooks, and mixins into Minecraft, NeoForge, LDLib, Registrate, recipe viewers, and map mods. |
| Shared helpers | `src/main/java/com/gregtechceu/gtceu/utils` | Identifier helpers, transfer utilities, data helpers, memoization, codecs, input sync, math, strings, and dev reload helpers. |
| Minecraft compatibility shims | `src/minecraftCompat/java` | Source-set shims for APIs or class names that moved during the 26.1.2 port, such as `ResourceLocation`, `GuiGraphics`, and `INBTSerializable`. |
| NeoForge compatibility shims | `src/main/java/net/neoforged/neoforge/client/model` | Local copies or compatibility surfaces for client model generator/geometry APIs expected by GTCEu and Registrate-style code. |
| LDLib compatibility shims | `src/main/java/com/lowdragmc` | Local compatibility code used while carrying LDLib-dependent behavior through the port. |


## Source Sets

The source sets are configured in `build.gradle`.

| Source set | Role |
| --- | --- |
| `main` | Common mod code and resources. Its resources also include `src/generated/resources`. |
| `minecraftCompat` | Compatibility code that is compiled before `main` and added to the main compile classpath. |
| `client` | Client-only code that is included in the GTCEu mod definition for client run configs. |
| `test` | GameTests and test-only support code. NeoForge modding dependencies are added to this source set. |
| `extra` | Extra runtime/dev code used by the expanded development run configs. |
| `clientExtra` | Client run source set that combines client and extra runtime classpaths. |

The `neoForge.mods` block in `gradle/scripts/moddevgradle.gradle` binds these source sets to the same `gtceu` mod for
development runs. This means run configuration behavior can differ depending on which source set the run uses.


## Resources and Generated Data

| Path | Purpose |
| --- | --- |
| `src/main/resources` | Checked-in runtime resources: assets, data, mixin config, access transformer, enum extensions, KubeJS plugin metadata, and the `neoforge.mods.toml` template output. |
| `src/main/templates` | Gradle-expanded templates, especially `META-INF/neoforge.mods.toml`. |
| `src/generated/resources` | Checked-in datagen output. Regenerate it with `runData` when possible instead of hand-editing it. |
| `src/test/resources/data/gtceu/structure` | GameTest structure templates used by the NeoForge test server. |
| `docs/neoforged` | Vendored NeoForged documentation snapshot for this port. Use it before web searches. |

GTCEu also builds dynamic runtime packs:

- `GTDynamicResourcePack` serves generated client resources such as runtime blockstates and item/model definitions.
- `GTDynamicDataPack` serves generated server data such as dynamic recipes and loot.
- `CommonProxy.registerPackFinders` adds both packs through `AddPackFindersEvent`.
- `ModelManagerMixin` posts `RegisterDynamicResourcesEvent` so client models can be regenerated during resource reloads.
- `ReloadableServerResourcesMixin` injects GT dynamic recipes and loot during server resource reload.


## Common Change Starting Points

| Task | Start here | Then check |
| --- | --- | --- |
| Add a normal item | `common/data/GTItems.java` | Components in `common/data/item/GTDataComponents.java`, models in `common/data/models`, tags in `data/tags`. |
| Add material-generated content | `common/data/GTMaterials.java` and `api/data/tag/TagPrefix.java` | Generation in `GTMaterialItems`, `GTMaterialBlocks`, and the late `RegisterEvent` flow in `CommonProxy`. |
| Add a simple machine | `common/data/GTMachines.java` | Helpers in `common/data/machines/GTMachineUtils.java`, machine classes in `api/machine` or `common/machine`, recipe type in `GTRecipeTypes`. |
| Add a multiblock | `common/data/machines/GTMultiMachines.java` | Pattern predicates in `api/pattern`, controller/part classes under `common/machine/multiblock`, tests under `src/test`. |
| Add a recipe type | `common/data/GTRecipeTypes.java` | Capabilities in `GTRecipeCapabilities`, serializers in `GTRecipeSerializers`, UI textures/builders in `api/recipe/ui`. |
| Add generated recipes | `data/recipe` | Run `runData`; avoid editing generated JSON unless the generator cannot express the data yet. |
| Add storage or automation behavior | `api/machine/trait`, `api/capability`, `api/transfer` | Capability registration in `CommonProxy.registerCapabilities` and adapters in `api/misc/forge`. |
| Add a GUI | `api/gui`, `api/gui/factory`, `api/gui/fancy` | Existing `*UI.java` companions beside machines/covers/items. |
| Add integration behavior | `integration/<modid>` | Guard with `GTCEu.Mods.is...Loaded()` or the integration API's plugin discovery. |
| Add a GameTest | `src/test/java/com/gregtechceu/gtceu/gametest` | Register the class in `GTGameTestBootstrap.TEST_CLASSES`, add `.nbt` templates under `src/test/resources/data/gtceu/structure`. |


## Basic Conventions

- Use `GTCEu.id("path")` for GTCEu identifiers. It applies the `gtceu` namespace and normalizes uppercase paths.
- Prefer `GTRegistration.REGISTRATE` and existing `GTMachineUtils` helpers for registries and generated content.
- Keep common logic out of client-only classes. Use `GTCEu.isClientSide()` only for physical-side checks and
  `GTCEu.isClientThread()` when checking the actual client thread.
- Put API contracts and reusable base types in `api`; put built-in content in `common`; put mod integration code in
  `integration`.
- Check `docs/neoforged/docs` before browsing for NeoForge API behavior.
