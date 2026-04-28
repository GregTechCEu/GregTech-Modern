---
title: Lifecycle and Registration
---


# Lifecycle and Registration

GTCEu depends heavily on ordered registration. Many familiar 1.20.1 classes still exist, but the 26.1.2 port routes
them through ModDevGradle, NeoForge mod bus events, data components, dynamic packs, and a custom registry queue.


## Startup Flow

| Stage | Code | What happens |
| --- | --- | --- |
| Mod construction | `GTCEu.GTCEu(IEventBus, FMLModContainer)` | Initializes config, material manager, high tier settings, dev defaults, common proxy, network payload registration, and test bootstrap hook. |
| Common proxy init | `CommonProxy.init(IEventBus)` | Registers common listeners, initializes UI factories when LDLib is loaded, prepares datagen providers, registers GT custom registries, registers Registrate listeners, initializes creative tabs and attachments. |
| Custom registry creation | `CommonProxy.registerRegistries` | Sends every registry from `GTRegistries.getRegistries()` to NeoForge through `NewRegistryEvent`. |
| Static content registration | `CommonProxy.onRegister(RegisterEvent)` | Runs once and initializes elements, materials, tag prefixes, sounds, blocks, fluids, recipe capabilities, recipe types, items, machines, covers, entities, ingredients, serializers, commands, effects, particles, worldgen, input sync, and cache hooks. |
| Late registry-specific generation | `CommonProxy.onRegisterLate(RegisterEvent)` | Handles content that depends on a particular registry becoming active, such as material fluids, material blocks, material items, pipes, wires, and block entity types. |
| Datapack registries | `CommonProxy.registerDataPackRegistries` | Registers ore vein, bedrock fluid, and bedrock ore registries with codecs for datapack loading. |
| Registry callbacks | `CommonProxy.modifyRegistries` | Adds bake callbacks for materials and machines. Material bake closes the material registry and fires post-material events. Machine bake updates render states. |
| Common setup | `CommonProxy.commonSetup` | Enqueues map ingredient converters for recipe lookup and optional CC: Tweaked integration setup. |
| Capability registration | `CommonProxy.registerCapabilities` | Registers item and block capability providers for bottles, quantum tanks, pipes, cables, machines, tools, drums, and optional FE/EU conversion. |
| Pack discovery | `CommonProxy.registerPackFinders` | Adds dynamic client resource and server data packs. |


## Registry Queueing

`GTRegistries` creates GTCEu's static registries and buffers entries until NeoForge opens registration.

The important pieces are:

- `GTRegistries.makeRegistry` creates synchronized or unsynchronized `MappedRegistry` instances with `RegistryBuilder`.
- `GTRegistries.register` either registers immediately or queues entries in `TO_REGISTER`.
- `GTRegistries.init` adds a high-priority listener that unfreezes registration and a low-priority listener that flushes
  the queue.
- `GTRegistries.builtinRegistry()` returns the frozen server registry access, or the client connection registry access
  on the client thread.

This lets static registration classes keep the older GTCEu shape while still respecting NeoForge registry timing.


## Content Ordering Rules

Some content must happen before other content:

- Materials are initialized before tag prefixes and material-generated content.
- `PostMaterialEvent` and KubeJS material modification fire after the material registry closes.
- Material fluids are generated during fluid registry registration.
- Material blocks, ores, indicators, cables, and pipes are generated during block registry registration.
- Material items, tools, and armor are generated during item registry registration.
- Machine render states are baked after machine registry bake.

When adding content, prefer the existing init method for that content family. Avoid force-loading unrelated registration
classes from static initializers because it can move the registration order.


## Client Flow

Client-only setup starts from `GTCEuClient`, a second `@Mod` entry point restricted to `Dist.CLIENT`.

`ClientProxy.init` registers client mod bus listeners and initializes client-only systems:

- map caches and render layers for ore veins and bedrock fluids;
- cape registration;
- dynamic machine render types;
- entity renderers, item decorations, key mappings, GUI layers, and particle providers;
- client payload handlers for client-executed bidirectional packets;
- model loaders for machines, pipes, and facades;
- debug overlay entries;
- item tint sources and item model property registrations;
- dynamic resource regeneration hooks.

General client event handling lives in `ClientEventListener`, which is subscribed through `@EventBusSubscriber` for
client runtime events such as render stages, FOV modification, client ticks, command registration, recipe sync, and cache
cleanup.


## Data Generation Flow

`GregTechDatagen.initPre()` runs before content loads. It sets JSON indentation, replaces the default Registrate model
provider with `GTModelProvider`, and adds blockstate generation through `BlockstateModelLoader`.

`GregTechDatagen.initPost()` runs after content registration and adds tag, language, and data map providers:

- block tags;
- item tags;
- fluid tags;
- entity tags;
- language entries;
- data maps.

The `runData` task writes to `src/generated/resources` and receives both `src/main/resources` and generated resources as
inputs through the NeoForge data run configuration.


## Network Flow

`GTCEu` registers `GTNetwork.registerPayloads` on the mod bus. Payload classes implement `CustomPacketPayload` and expose
both a `TYPE` and a `StreamCodec`.

Server/client direction is declared in one place:

- `playToServer` for client requests;
- `playToClient` for server updates;
- `playBidirectional` for packets that can travel both ways.

Client execution handlers that need client-only classes are registered separately in
`ClientProxy.registerClientPayloadHandlers`.


## Practical Rules for Contributors

- Register new static GT registries in `GTRegistries` and add them through the existing `getRegistries()` flow.
- Register new NeoForge deferred registers on the mod bus from common init, as `GTAttachmentTypes` and
  `GTDataComponents` do.
- Keep generated material content in the late registry branches instead of eager static blocks.
- Use `event.enqueueWork` from setup events when work must happen after registry setup.
- Put client-only event listeners in `ClientProxy` or `ClientEventListener`, not common registration code.
- If a capability provider can appear, disappear, or change behavior at runtime, remember that NeoForge capability
  caches need invalidation from the owning block entity or level code.
