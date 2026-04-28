---
title: Systems and Extension Points
---


# Systems and Extension Points

Most GTCEu changes fall into a few large systems. Use this page to find the owning package and the pattern to copy.


## Machines

Machine definitions are registered content. Machine classes are runtime behavior.

| Layer | Main paths | Notes |
| --- | --- | --- |
| Definitions | `common/data/GTMachines.java`, `common/data/machines` | Registers simple machines, generators, multiblocks, hatches, buses, research machines, AE2 machines, and GCYM machines. |
| Helpers | `common/data/machines/GTMachineUtils.java` | Prefer these helpers for tiered machines, steam machines, generators, transformers, battery buffers, laser hatches, and tiered multiblocks. |
| Base runtime | `api/machine` | `MetaMachine`, `MachineDefinition`, tiered machines, steam machines, multiblock bases, feature interfaces, and reusable traits. |
| Built-in runtime | `common/machine` | Concrete electric, steam, storage, multiblock, owner, and trait behavior. |
| Block entity bridge | `api/blockentity`, `common/data/GTBlockEntities.java` | Machine blocks store `MetaMachine` behavior through block entities and attach capabilities through the block/machine layer. |

For a normal electric machine, start with `GTMachineUtils.registerSimpleMachines`. It connects a recipe type, tier list,
machine factory, model, tooltip, and default storage shape. For multiblocks, start in `GTMultiMachines` and reuse pattern
predicates from `api/pattern`.


## Recipes

The recipe system is capability-oriented. Items, fluids, EU, computation, and other inputs/outputs are modeled as
`RecipeCapability` entries instead of hard-coded item/fluid slots everywhere.

| Layer | Main paths | Notes |
| --- | --- | --- |
| Recipe types | `common/data/GTRecipeTypes.java` | Defines IO sizes, EU direction, overlays, progress bars, custom builders, custom logic, and generated recipe side effects. |
| Capabilities | `common/data/GTRecipeCapabilities.java`, `api/capability/recipe` | Defines what content type a recipe can consume or produce. |
| Runtime recipe | `api/recipe/GTRecipe.java` | Holds content maps, conditions, duration, EU, chanced outputs, and serialized data. |
| Builder/datagen | `data/recipe/builder/GTRecipeBuilder.java`, `data/recipe` | Used by generated recipes and many modpack-facing APIs. |
| Lookup | `api/recipe/lookup` | Converts ingredients and machine contents into searchable map ingredients. Converters are registered in common setup. |
| Execution | `api/machine/trait/RecipeLogic.java` | Finds, consumes, ticks, and completes recipes for workable machines. |

When adding a recipe capability or custom ingredient, update both serialization and recipe lookup. Existing tests under
`src/test/java/com/gregtechceu/gtceu/api/recipe` are good examples of the edge cases that usually break.


## Materials and Tag Prefixes

Materials drive a large amount of generated content.

| Concept | Main paths | Notes |
| --- | --- | --- |
| Elements | `common/data/GTElements.java` | Element definitions used by materials. |
| Materials | `common/data/GTMaterials.java`, `api/data/chemical/material` | Material objects, properties, registry, stacks, events, and addon hooks. |
| Tag prefixes | `api/data/tag/TagPrefix.java` | Defines forms such as dusts, plates, rods, wires, pipes, ores, and generated item/block behavior. |
| Material items | `common/data/GTMaterialItems.java` | Generates item forms and tools during item registry registration. |
| Material blocks | `common/data/GTMaterialBlocks.java` | Generates compressed blocks, ores, indicators, cables, wires, and pipes during block registry registration. |
| Material fluids | `common/data/GTFluids.java` | Generates material fluids during fluid registry registration. |
| Language | `data/lang/MaterialLangGenerator.java` | Adds generated material localization and lets addons override by namespace. |

The material registry is closed before `PostMaterialEvent`. Addons and KubeJS material modification must happen in the
expected material events rather than after generated content has already been created.


## Storage, Transfer, and Capabilities

GTCEu has its own machine traits and recipe handlers, while also exposing NeoForge capabilities for automation and mod
compatibility.

| Area | Main paths | Notes |
| --- | --- | --- |
| Machine traits | `api/machine/trait` | Item, fluid, energy, computation, laser, hazard, and proxy traits. Many implement `ICapabilityTrait`. |
| Recipe handlers | `api/capability/recipe`, `api/recipe` | Machine recipe IO is exposed as `IRecipeHandler` lists grouped by `IO` and `RecipeCapability`. |
| Transfer helpers | `api/transfer`, `utils/GTTransferUtils.java` | Shared movement and simulation utilities. |
| NeoForge adapters | `api/misc/forge` | Bridges older GTCEu-style handlers with NeoForge item, fluid, and energy APIs. |
| Capability registration | `CommonProxy.registerCapabilities` | Attaches providers to item stacks and blocks during `RegisterCapabilitiesEvent`. |
| Pipenets | `api/pipenet`, `common/pipelike` | Cable, fluid pipe, item pipe, laser pipe, optical pipe, and duct behavior. |

When changing capability shape, check both the block/item provider and any cache invalidation path. NeoForge caches
capability lookups aggressively.

Entity/player state that belongs to a holder should usually use NeoForge attachments. GTCEu's current attachment
registration is in `common/data/GTAttachmentTypes.java`.


## Sync and Save Data

GTCEu's managed sync system lives under `api/sync_system`.

| Area | Main paths | Notes |
| --- | --- | --- |
| Managed objects | `ISyncManaged`, `SyncDataHolder`, `ClassSyncData`, `FieldSyncData` | Classes expose a sync holder and field metadata for save/sync handling. |
| Block entities | `ManagedSyncBlockEntity` | Block entities that participate in the system inherit this base and must tick their sync data. |
| Annotations | `api/blockentity`, `api/sync_system` users | Existing code uses annotations such as `@SaveField`, `@SyncToClient`, and rerender/update hooks. |
| Documentation | `Development/Data-Sync-System` | The dedicated sync docs explain usage, annotations, and migration from LDLib SyncData. |

Use the managed sync system for GTCEu machine/block entity state that must save, sync, or trigger client rerenders.
Use item data components for item stack state.


## GUI and UI

GTCEu still uses LDLib for most modular UI work, but the port separates more UI construction into companion `*UI.java`
classes.

| Area | Main paths | Notes |
| --- | --- | --- |
| UI factories | `api/gui/factory` | Registers machine, cover, and held-item UI factories through `GTUIFactories.init()`. |
| Generic widgets | `api/gui/widget`, `api/gui/fancy` | Reusable widgets, fancy tab/page UI, configurator panels, phantom slots, tanks, and recipe widgets. |
| Machine UI | `api/machine/*UI.java`, `common/machine/**/*UI.java` | Companion classes build UI widgets or `ModularUI` instances for concrete machines. |
| Cover UI | `common/cover/**/*UI.java`, `api/cover/filter/*UI.java` | Cover behavior classes usually delegate UI creation to a companion class. |
| Held item UI | `api/gui/factory/GTHeldItemUIFactory.java` | Items can implement `IGTHeldItemUI` to open a held-item UI. |
| Recipe viewer UI | `api/recipe/ui`, `integration/jei`, `integration/emi`, `integration/rei`, `integration/xei` | Shared recipe display widgets and per-viewer plugin code. |

If a machine implements `IFancyUIMachine`, its `createUIWidget` generally returns a page/widget for
`FancyMachineUIWidget`. Simple LDLib screens can still return a full `ModularUI` from `createUI`.


## Client Models and Rendering

Client rendering is split between generated model data, dynamic runtime resources, and custom renderers.

| Area | Main paths | Notes |
| --- | --- | --- |
| Client entry | `client/GTCEuClient.java`, `client/ClientProxy.java` | Registers model loaders, renderers, particles, item decorators, tint sources, and dynamic resource listeners. |
| Model loaders | `client/model/machine`, `client/model/pipe`, `client/model/item` | Custom unbaked/baked models for machines, pipes, facades, and item definitions. |
| Model generation | `common/data/models`, `data/model`, `api/registry/registrate/provider` | Datagen and runtime generators for machine, pipe, blockstate, item, and material models. |
| Dynamic renders | `client/renderer/machine` | Runtime render components such as tanks, chests, fusion rings, boilers, fluid areas, growing plants, and central monitor. |
| Item overlays | `client/renderer/item/decorator` | Tool bars, tank previews, component overlays, and lamp overlays. |

The 26.x item model system uses item definitions, tint sources, and range/conditional item model properties. Do not add
new legacy item property predicates in `GTItems.modelPredicate`; those methods are intentionally no-ops in this port.


## Integrations

Optional integration code lives under `integration`.

| Package | Purpose |
| --- | --- |
| `ae2` | Applied Energistics machine and pattern-buffer integration. |
| `cctweaked` | ComputerCraft/CC: Tweaked peripherals and setup. |
| `create` | Create/Ponder hooks. |
| `emi`, `jei`, `rei`, `xei` | Recipe viewer integrations and shared display widgets. |
| `jade` | Jade/WAILA-style providers. |
| `kjs` | KubeJS startup events, builders, recipe schemas, validators, and script-facing helpers. |
| `map` | Shared map cache/rendering plus FTB Chunks, JourneyMap, and Xaero integrations. |
| `modernfix` | ModernFix client integration metadata and hook. |

Guard optional code with `GTCEu.Mods.is...Loaded()` or the integration API's own plugin discovery. Optional test classes
must also be guarded, as `GTGameTestBootstrap` does for the AE2 pattern buffer tests.


## Mixins

Mixin targets are declared in `src/main/resources/gtceu.mixins.json` and implemented under `core/mixins`.

The mixins cover:

- Minecraft runtime hooks for recipes, registries, tags, world loading, chunk generation, redstone, block entities, and
  client rendering;
- NeoForge model/data generation accessors;
- LDLib and Registrate compatibility hooks;
- recipe viewer hooks for EMI, JEI, and REI;
- map mod hooks for FTB Chunks and Xaero;
- dev-only resource reload hooks.

When adding a mixin, keep the target narrow, document why an event or API hook is not enough, and check both production
and datagen runs. Many mixins exist only because GTCEu generates content at a scale or timing that ordinary hooks do not
cover.


## Metadata Hooks

The port also relies on metadata files that change Minecraft or NeoForge surfaces before normal code runs.

| File | Purpose |
| --- | --- |
| `src/main/resources/META-INF/accesstransformer.cfg` | Opens otherwise inaccessible members. |
| `src/main/resources/META-INF/enum_extensions.json` | Declares enum extensions consumed by NeoForge. |
| `injected_interfaces/interfaces.json` | Injects GTCEu interfaces into target classes such as `MappedRegistry` and `BakedQuad`. |
| `src/main/resources/gtceu.mixins.json` | Declares mixins and the `GTMixinPlugin`. |

Use these mechanisms sparingly. They are powerful enough to make porting possible, but they also create hidden coupling
that future contributors need to understand.
