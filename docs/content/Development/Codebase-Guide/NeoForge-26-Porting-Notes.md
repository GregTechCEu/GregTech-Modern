---
title: NeoForge 26 Porting Notes
---


# NeoForge 26 Porting Notes

This page collects the porting differences that most often surprise contributors coming from GTCEu 1.20.1 or older
Forge/NeoForge branches.


## Current Port Baseline

| Item | Current value or location |
| --- | --- |
| Minecraft version | `26.1.2` in `gradle/libs.versions.toml`. |
| NeoForge version | `26.1.2.29-beta` in `gradle/libs.versions.toml`. |
| Loader version | `4` in `gradle/libs.versions.toml`. |
| Build plugin | `net.neoforged.moddev` from ModDevGradle. |
| Java toolchain | `JavaLanguageVersion.of(25)` in `build.gradle`. |
| Kotlin bytecode target | `JvmTarget.JVM_22` in `build.gradle`. |
| Local NeoForge docs | `docs/neoforged/docs`, snapshot version `26.1`. |

!!! warning "Check the build files, not old branch memory"

    Older GTCEu branches and some older docs may say Java 17, Java 21, Architectury, or Forge-era APIs. For this branch,
    `build.gradle`, `dependencies.gradle`, `gradle/libs.versions.toml`, and `gradle/scripts/moddevgradle.gradle` are the
    source of truth.


## Changes to Watch For

| Area | 26.x pattern in this repo | Older assumption to avoid |
| --- | --- | --- |
| Identifiers | Use `GTCEu.id(...)` and `net.minecraft.resources.Identifier`. | Hand-building namespace strings or assuming every old `ResourceLocation` call site still exists unchanged. |
| Build setup | ModDevGradle config in `gradle/scripts/moddevgradle.gradle`. | Architectury plugin behavior from 1.20.1-era docs. |
| Compatibility shims | `src/minecraftCompat/java` contains temporary source-set aliases for moved APIs. | Treating those aliases as a reason to use old APIs in new code. |
| Data components | Register item state in `common/data/item/GTDataComponents.java` with persistent codecs and optional network `StreamCodec`s. | Storing all item state in ad hoc stack NBT. |
| Networking | Payload classes implement `CustomPacketPayload` and expose `TYPE` plus `StreamCodec`; `GTNetwork` registers directions. | Legacy simple-channel packet registration or packets without explicit stream codecs. |
| Client packet handling | Client-only payload execution is registered in `ClientProxy.registerClientPayloadHandlers`. | Referencing client classes from common packet registration. |
| Capabilities | Providers are attached in `RegisterCapabilitiesEvent`; lookups use `level.getCapability(...)` or item capability APIs. | Direct interface checks or old lazy-optional patterns. |
| Runtime assets | Client resources are regenerated through `RegisterDynamicResourcesEvent` and `GTDynamicResourcePack`. | Assuming every model/blockstate exists as checked-in JSON. |
| Runtime data | Dynamic recipes and loot go through `GTDynamicDataPack` during server resource reload. | Assuming all server data comes from `src/generated/resources`. |
| Item models | Item model behavior uses item definitions, tint sources, and range/conditional model properties. | Adding legacy item property predicates through `GTItems.modelPredicate`, which is now a no-op. |
| NBT serialization | GTCEu NBT serializers receive registry lookup context where required. | Calling old NBT helpers that cannot resolve registry-backed values. |
| Attachments | Entity/player state such as medical hazards uses NeoForge attachments. | Reintroducing global maps or custom player save hooks for state that belongs on an entity. |
| GameTests | `GTGameTestBootstrap` reflects GTCEu annotations into NeoForge 26.1.2 test instances. | Relying only on old Minecraft `@GameTest` discovery. |
| Model generator APIs | Some model generator/geometry APIs are locally shimmed under `src/main/java/net/neoforged/neoforge/client/model`. | Treating those packages as upstream dependencies that can be removed without checking consumers. |
| Metadata | Enum extensions, interface injection, and access transformers are active porting surfaces. | Assuming all vanilla/NeoForge shape changes can be solved in normal Java code. |


## Identifier and Registry Access

Use `GTCEu.id("path")` for GTCEu-owned identifiers. It handles three important cases:

- blank paths return the mod namespace template;
- `namespace:path` values are passed through;
- unnamespaced paths are normalized to lower underscore and put under `gtceu`.

For registry access, use `GTRegistries.builtinRegistry()` when code needs the frozen registry view that works on both
server and client. On the client thread, it prefers the connection registry access.


## Data Components

Data components are the main port-era replacement for many item-state patterns.

Good examples live in `GTDataComponents`:

- tool data and tool behavior components;
- material identity on item stacks;
- armor data and fly mode;
- filter configuration;
- fluid and energy contents;
- binding, data-copy, monitor, and computer monitor cover state;
- creative machine info and facade/lamp state.

Component value types should have stable codecs and should be treated as immutable while stored. If the value is sent to
the client, add a `networkSynchronized` codec. If it is save-only, a persistent codec may be enough.

The local NeoForge reference page is `docs/neoforged/docs/items/datacomponents.md`.


## Registry-Aware NBT

The port has GTCEu NBT helpers under `api/nbt`, and save/load paths increasingly need registry lookup context for
registry-backed values. Block entity save/load context is tracked through core mixin helpers and consumed by managed sync,
item handlers, and fluid tanks.

When adding serialized state:

- prefer a `Codec` or data component when the value belongs on an item stack;
- pass the available `HolderLookup.Provider` through NBT serializers instead of reaching for global registries;
- check `ManagedSyncBlockEntity`, `CustomItemStackHandler`, and `CustomFluidTank` for current save/load patterns.


## Attachments

NeoForge attachments are used for state that belongs to an entity or other attachable holder. GTCEu registers attachment
types in `common/data/GTAttachmentTypes.java`; the current medical/hazard tracker is attached to players through
`MedicalConditionTracker`.

Use attachments for attachable persistent state before adding static maps, custom player serialization hooks, or
side-channel caches. The local NeoForge reference page is `docs/neoforged/docs/datastorage/attachments.md`.


## Compatibility Shims

The `minecraftCompat` source set exists to keep migrated code compiling while APIs settle across the port. It provides
small aliases for moved or renamed APIs, including classes under `net.minecraft.resources`, `net.minecraft.client.gui`,
and `net.neoforged.neoforge.common.util`.

Use these shims as a migration aid, not as the preferred style for new code. New code should follow the real 26.x APIs
used by the owning package unless the compatibility source set is deliberately part of that package's pattern.


## Model and Datagen Shims

The port also carries compatibility surfaces for model generation. `utils/data/ExistingFileHelper.java` is marked as a
temporary compatibility shim for the old model generator surface while GTCEu migrates toward the 26.1 vanilla model
provider API. Related generator classes live under `src/main/java/net/neoforged/neoforge/client/model/generators` and
`src/main/java/net/neoforged/neoforge/client/model/geometry`.

Before removing or bypassing these shims, check:

- `api/registry/registrate/provider`;
- `common/data/models`;
- `data/model/builder`;
- runtime providers such as `RuntimeBlockstateProvider`.

The local NeoForge reference page is `docs/neoforged/docs/resources/client/models/datagen.md`.


## Networking and Stream Codecs

Packets live under `common/network/packets`. A typical packet has:

- a static `Identifier ID`;
- a static `CustomPacketPayload.Type<T> TYPE`;
- a static `StreamCodec<ByteBufLike, T> CODEC`;
- an `execute` method registered by `GTNetwork` or `ClientProxy`.

Choose the narrowest useful buffer type:

- `ByteBuf` for primitive-only payloads;
- `FriendlyByteBuf` for vanilla buffer helpers;
- `RegistryFriendlyByteBuf` when decoding registry-backed values.

The local NeoForge references are `docs/neoforged/docs/networking/payload.md` and
`docs/neoforged/docs/networking/streamcodecs.md`.


## Capabilities and Adapters

NeoForge capabilities are registered in `CommonProxy.registerCapabilities`.

GTCEu attaches providers for:

- fluid item handling on bottles and quantum tanks;
- GT energy exposure over FE where configured;
- pipe, cable, laser, duct, optical, and machine blocks;
- component items, GT tools, and drums.

Adapter classes under `api/misc/forge` bridge GTCEu handlers with NeoForge item, fluid, and energy APIs. Use those
adapters instead of copying conversion code.

The local NeoForge reference page is `docs/neoforged/docs/inventories/capabilities.md`.


## Metadata and Core Access

Some 26.1.2 porting work depends on metadata files rather than ordinary registration code:

- `src/main/resources/META-INF/enum_extensions.json` declares enum extensions.
- `injected_interfaces/interfaces.json` injects GTCEu interfaces into Minecraft classes.
- `src/main/resources/META-INF/accesstransformer.cfg` opens members needed by the port.
- `src/main/resources/gtceu.mixins.json` lists active mixins.
- `core/mixins/GTMixinPlugin.java` conditionally enables dev and optional-mod compatibility mixins.

Treat these as reviewed architecture decisions. If a new hook seems to require one of them, document why an event,
capability, accessor, or normal API was not enough.


## Dynamic Resources and Data

The port keeps generated content split into two categories:

- persistent generated data from `runData`, checked into `src/generated/resources`;
- runtime generated data/assets served by dynamic packs.

Dynamic client assets are rebuilt on model/resource reload. Important hooks include:

- `ModelManagerMixin`, which posts `RegisterDynamicResourcesEvent`;
- `ClientProxy.preRegisterDynamicAssets`;
- `ClientProxy.registerDynamicAssets`;
- `ClientProxy.postRegisterDynamicAssets`;
- `RuntimeBlockstateProvider` and `RuntimeExistingFileHelper`.

Dynamic server data is injected through `ReloadableServerResourcesMixin`. This is where GT dynamic recipes and generated
loot are added to the server reload flow.

When debugging missing assets, check whether the asset should be checked in, generated by `runData`, or supplied at
runtime by `GTDynamicResourcePack`.


## GameTests

The branch uses custom annotations in `src/test/java/com/gregtechceu/gtceu/gametest/annotation` and reflects them into
NeoForge 26.1.2 `GameTestInstance`s.

Important differences from older tests:

- `GTCEu.registerGameTestBootstrap` only initializes tests when the `TEST` environment variable is present.
- The `gameTestServer` and `gameTestClient` run configs set `TEST=true`.
- Test classes must be listed in `GTGameTestBootstrap.TEST_CLASSES`, unless they are optional and added by
  `addOptionalTest`.
- Structure templates live under `src/test/resources/data/gtceu/structure`.
- The bootstrap wraps legacy `GameTestHelper` and `ExtendedGameTestHelper` signatures so existing tests can keep a
  familiar method shape.

Run `.\gradlew.bat runGameTestServer` before pull requests that touch machines, recipes, covers, storage, data behavior,
or anything with world interaction.


## Porting Checklist

When moving a feature from an older branch:

1. Find the owning system in [Systems and Extension Points](Systems-and-Extension-Points.md).
2. Replace old id construction with `GTCEu.id` or the target API's `Identifier` helpers.
3. Move item stack state into a data component if the state must persist or sync.
4. Replace old packet registration with a `CustomPacketPayload` and `StreamCodec`.
5. Register capabilities in `RegisterCapabilitiesEvent`; use GTCEu adapters where possible.
6. Decide whether assets/data are checked in, datagen output, or dynamic pack output.
7. Add or update GameTests, then register the test class in `GTGameTestBootstrap`.
8. Run `runData` when generators changed, `spotlessCheck` for formatting, and `runGameTestServer` for behavior.
