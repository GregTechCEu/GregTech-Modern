Status: ready-for-agent

# GTM 26.1.2 NeoForge 移植 Spec

## Problem Statement

当前仓库是 GregTech Modern（GTM）的 MC 1.20.1 Forge 基线，相对上游 `GregTechCEu/GregTech-Modern` 的 1.20.1 线没有本地改动。用户希望把它移植到 Minecraft 26.1.2 + NeoForge 26.1.2.x，同时能够持续跟踪上游 1.20.1 线的修复和演进。

移植不能只停留在替换 Forge 包名：目标版本同时带来 Java/Gradle 工具链变化、NeoForge API 变化、网络 payload 重写、传输与 capability 事务模型、Data Component、datapack registry、datagen、两阶段客户端渲染和 mixin 目标变化。当前依赖列表中还有一些依赖没有 26.1.2 构件，ModularUI 没有 26.1.2 发布版，但 GTM 大量 UI、recipe viewer 和 cover 代码直接使用它。

## Solution

建立一个 MC 26.1.2 + NeoForge 26.1.2.x 的 GTM 移植分支，并把 ModularUI-Modern 的 1.21.1 分支完整移植为仓库内独立模块。GTM 以已核验的 26.1.2 依赖为目标坐标，移除没有官方 26.1.2 支持的可选集成，保留核心机器、材料、配方、数据生成、UI、网络同步和已确认可用的第三方集成。

实现按以下 task graph 执行：先建立工具链和上游同步基线；随后并行处理完整 MUI 模块与核心 NeoForge API；再分别迁移数据、传输、网络、客户端渲染和集成；最后通过现有 Gradle build、data generation、GameTest server、clean client/server 运行入口完成验收。

上游没有 MC 26.x 分支。`1.20.1` 分支是持续同步源，`1.21` 分支只作为 Forge→NeoForge 的翻译手册。同步时不能直接 cherry-pick 1.20.1 提交到 26.1.2 分支，必须按 commit group 翻译、验证并保持移植分支的提交边界清晰。

## User Stories

1. As a GTM player, I want the mod to load on Minecraft 26.1.2 with NeoForge, so that I can use GTM in a current 26.1.2 modpack.
2. As a server operator, I want a dedicated server to start with GTM without client-only class failures, so that the same build works in a server environment.
3. As a mod developer, I want the Gradle project to resolve the exact 26.1.2 and NeoForge dependencies, so that local and CI builds are reproducible.
4. As a mod developer, I want the project to use the Java and Gradle toolchain required by MC 26.1.2, so that compilation does not depend on an accidental local setup.
5. As a GTM maintainer, I want a complete ModularUI API available inside the repository, so that existing GTM screens, widgets, recipe viewers, covers and synchronization code do not need to be reduced to a small substitute API.
6. As a GTM maintainer, I want the MUI module to remain independently bounded from GTM code, so that future MUI fixes can be reviewed and updated separately.
7. As a GTM user, I want registered GTM items, blocks, fluids, machines, materials and custom registries to load normally, so that existing worlds and recipes can be opened after the port.
8. As a GTM developer, I want NeoForge mod-bus and game-bus events to be registered through the target API, so that initialization order remains deterministic.
9. As a GTM developer, I want custom registries and deferred registries to be created and frozen correctly, so that registry synchronization does not fail at startup.
10. As a GTM user, I want existing recipes and recipe ingredients to retain their matching behavior, so that machines produce the same results after the port.
11. As a GTM developer, I want NBT-based ingredient behavior represented through the target Data Component and codec APIs, so that recipes remain data-driven on 26.1.2.
12. As a pack author, I want ore veins, bedrock ores, bedrock fluids and other datapack registries to be generated in the target format, so that custom world-generation data remains extensible.
13. As a pack author, I want generated tags, loot tables, recipes and models to be valid for 26.1.2, so that data generation produces loadable resources.
14. As a GTM user, I want item, fluid and energy transport in machines, pipes and covers to preserve insertion, extraction, simulation and transaction semantics, so that automation networks behave as before.
15. As a GTM developer, I want the GTM capability boundary to adapt to NeoForge capabilities and the new transfer API, so that external mods can still discover supported machine capabilities.
16. As a GTM user, I want machine GUI state, cover state, recipe state and server-to-client updates to synchronize reliably, so that screens do not display stale or invalid data.
17. As a GTM developer, I want all network messages to use the target payload and `StreamCodec` model, so that networking remains compatible with NeoForge 26.1.2.
18. As a GTM user, I want machine blocks, block entities, items, entities and GUI elements to render correctly on a vanilla 26.1.2 client, so that the port does not depend on removed rendering mods.
19. As a GTM developer, I want mixins to target the 26.1.2 classes and mappings, so that startup does not fail because of stale Forge, Embeddium or old vanilla targets.
20. As a player using JEI, REI, Jade, AE2, AE2WTLib, KubeJS, FTB, CC:T, Curios, Xaero or JourneyMap, I want GTM integrations to load with the pinned 26.1.2 versions, so that supported interoperability remains available.
21. As a pack author, I want unsupported optional integrations to be absent cleanly, so that GTM does not crash when EMI, Create, Embeddium, Oculus, GameStages or unavailable development tools are not installed.
22. As a GTM user, I want the core mod to retain its behavior when optional integrations are absent, so that an optional dependency cannot become a hidden hard dependency.
23. As a GTM developer, I want existing recipe, machine, cover, AE2 and GameTest behavior tests to run on the target toolchain, so that the port can detect semantic regressions rather than only compilation errors.
24. As a GTM developer, I want data generation to run as a project-level verification seam, so that invalid codecs, registries, tags, loot and generated assets are caught before runtime testing.
25. As a GTM developer, I want a clean server and clean client smoke test, so that side-only class loading, mod metadata, dependency declarations and startup registration are verified together.
26. As an upstream maintainer, I want the port branch to record its upstream 1.20.1 commit baseline, so that future synchronization has an unambiguous source point.
27. As an upstream maintainer, I want upstream fixes to be replayed as translated commit groups, so that the 26.1.2 branch can follow upstream without pretending that incompatible commits cherry-pick cleanly.
28. As a contributor, I want implementation commits to keep MUI, GTM porting, dependency changes and optional integration removal separable, so that reviews and future rebases remain manageable.
29. As a project maintainer, I want CI and publishing metadata to identify MC 26.1.2 and NeoForge correctly, so that generated artifacts cannot be mistaken for the 1.20.1 Forge line.
30. As a project maintainer, I want AI-assisted contributions to follow the repository AI policy, so that upstream-facing work includes disclosure and human review.

## Implementation Decisions

- The target coordinate is Minecraft 26.1.2 with the latest verified stable NeoForge 26.1.2.x, currently `26.1.2.99`. The implementation must re-resolve the NeoForge release during bootstrap and record the exact resolved version.
- The target toolchain uses Java 25, Gradle 9.1 or newer, the 26.1-compatible ModDevGradle plugin, and official Mojang mappings. Parchment must not remain as a 26.1 mapping dependency.
- The GTM baseline is the current pristine 1.20.1 Forge source. The exact upstream `1.20.1` commit SHA used at bootstrap must be recorded before port changes begin.
- The upstream `1.20.1` line is the synchronization source. The upstream `1.21` line is a migration reference only; it is not the 26.1.2 implementation base.
- Synchronization uses an independent 26.1.2 port branch and translated commit groups. A source update is rebased into the port workflow, then manually translated and validated. MUI changes remain separate from GTM synchronization changes.
- Registrate is consumed as `com.tterrag.registrate:Registrate:MC26.1-1.5.0` from `https://maven.gegy.dev/releases/`. The MC 26.2-only `26.2-1.6.0` release is not used.
- Configuration is consumed as `dev.toma.configuration:configuration-neoforge:4.1.2+26.1.2` from the Toma Repsy repository, with its API and initialization behavior validated during the build.
- MixinExtras is not added as a separate old Forge dependency when the target NeoForge toolchain already provides the required support. Any explicit mixin annotation processing still required by the port must use a 26.1-compatible artifact.
- ModularUI-Modern is fully ported from its `1.21.1` branch into an independent Gradle module embedded in this repository. GTM consumes the module through a normal project dependency boundary, and the complete public API is preserved rather than replaced with GTM-specific stubs.
- The MUI module must compile independently before GTM integration work is considered complete. GTM UI integration must retain current screen, widget, value-sync, menu, drawable and recipe-viewer behavior.
- The pinned dependency set includes JEI `29.35.0.94`, REI `26.1.819+neoforge`, Jade `26.1.10`, Curios `15.0.0+26.1.2`, AE2 `26.1.11-beta`, AE2WTLib `26.1.1-beta`, KubeJS `26.1.2-8.0.4+neoforge`, Rhino `2101.2.8-build.91`, Architectury `20.0.12`, FTB Library `26.1.2.7`, FTB Teams `26.1.2.4`, FTB Quests `26.1.2.7`, FTB Chunks `26.1.2.8`, CC:T `v26.1.2-1.120.0`, Cloth Config `26.1.154`, Xaero Minimap `26.4.2`, Xaero WorldMap `1.45.0`, ModernFix `5.27.22+mc26.1.2`, KotlinForForge `6.3.0`, ResourcefulLib `4.0.1`, Bookshelf `26.1.2.15`, Spark `1.10.173`, JAVD/Trenzalore 26.1.x runtime files, and JourneyMap file `8768945`.
- JourneyMap API uses the verified 26.1 snapshot coordinate while the runtime mod uses CurseForge file `8768945`; dependency resolution and startup must verify that the API and runtime are compatible.
- Unverified carry-over file IDs are not silently retained as target pins. Observable, Argonauts, Heracles and WorldStripper are removed from the default target runtime because no 26.1.2 artifact was verified.
- Official EMI, Create/Ponder/Flywheel, Embeddium/Oculus and GameStages are cut from the target. Chapters is not introduced as an automatic replacement. The corresponding dependencies, metadata, run configurations and GTM integration modules are removed or isolated.
- No empty compatibility stubs are kept for cut integrations. Core GTM code must compile and run without them. JEI, REI, Jade, AE2, AE2WTLib, KubeJS, FTB, CC:T, Curios, Xaero and JourneyMap remain supported integration surfaces.
- The core API migration changes Forge event bus, lifecycle, registry, capability, fluid, energy, item, server lifecycle and client event boundaries to their NeoForge equivalents. Initialization order and side safety are part of the behavior contract.
- Custom registries and data-driven registries are migrated to the 26.1 NeoForge registration model. Ore veins, bedrock ores and bedrock fluids become valid datapack registries with generated data rather than legacy synchronization packets.
- NBT-sensitive recipe ingredients are migrated to Data Component and codec-based representations. Recipe serialization must preserve matching, copying, validation and network behavior.
- Loot, tags, data maps, models, recipes and datapack built-in entries are migrated to the 26.1 codec and datagen contracts. Loot type/function/condition registrations use the target MapCodec model.
- GTM's item, fluid and energy transport behavior is preserved at its public semantic boundary while implementation adapts to NeoForge's ResourceHandler, EnergyHandler and transaction model. Existing machine, pipe, cover and external capability behavior must be tested after adaptation.
- Every network message is migrated from SimpleChannel/FriendlyByteBuf conventions to CustomPacketPayload, Type, StreamCodec and payload handler registration. Direction, threading, optionality, size limits and server/client execution are explicit for each message family.
- Client work covers entity render state, block entity render state, GUI render state, special item models, particles, overlays, dynamic highlights and shader-compatible rendering. The first acceptance target is a vanilla 26.1.2 client; Embeddium/Oculus compatibility is not required.
- Mixin configurations, compatibility targets and injection points are reviewed against 26.1.2 mappings. Stale Forge, Embeddium and removed third-party targets are deleted rather than left to fail at runtime.
- The project-level Gradle lifecycle is the highest testing seam: compilation/package, data generation, GameTest server, clean dedicated server, and clean client. New tests should attach to these existing seams rather than inventing a parallel harness.
- Existing recipe, machine, cover, AE2 and GameTest tests are ported before being expanded. New tests cover network codec round trips, capability/transfer semantics, registry/datagen loading, MUI smoke behavior and optional-integration absence.
- CI and publishing metadata must report MC 26.1.2, NeoForge and the new Java toolchain; the old 1.20.1 Forge metadata must not remain on the target branch.

## Testing Decisions

- A good test observes external behavior: a registry is present, a recipe matches, a machine transfers the expected amount, a packet reconstructs the expected state, a generated resource loads, or a client/server starts. Tests must not assert private helper structure merely to protect a chosen implementation.
- The primary seam is the existing Gradle lifecycle. The implementation is complete only when the main build/package task, data generation run, GameTest server run, clean server run and clean client run all pass on the target toolchain.
- The existing unit and GameTest suites are prior art. Recipe lookup, recipe serializer, overclocking, machine parts, covers, AE2 pattern buffers, capability helpers and stress tests should remain the regression baseline.
- Core API and registry work is tested by build-time registration plus data generation and a clean server startup. This catches missing event subscribers, invalid registry keys, metadata errors and server-side class loading.
- Data and codec work is tested by round-trip serialization, generated-resource loading and representative recipe/loot/tag/datapack registry tests. Tests must include Data Component-backed ingredients and datapack registry entries.
- Transfer and capability work is tested through existing machine, pipe, cover and GameTest behavior, including simulated versus executed insertion/extraction, sided access, empty/full boundaries and transaction rollback/commit behavior.
- Networking is tested at the codec and handler boundary. Each packet family needs a representative encode/decode round trip and a server/client behavior test where the current suite already has a suitable GameTest seam.
- MUI is tested first as an independently compiling module, then through GTM UI smoke coverage: create a representative panel, open a representative machine/cover screen, synchronize a representative value, and render a representative recipe-viewer widget.
- Client work is tested with clean client startup and representative render paths. Where a headless render assertion is impractical, the acceptance requirement is no crash, correct resource/model resolution and a manual smoke check recorded with the implementation result.
- Optional integrations are tested in two modes: the supported dependency is present and initializes; the cut or absent dependency is absent and GTM still starts. No test may require a cut dependency to compile the core mod.
- CI tests should run the same Gradle entry points as local validation, with dependency resolution failures reported as first-class failures rather than silently falling back to 1.20.1 artifacts.

## Out of Scope

- Actual implementation is not performed by this spec-writing task; implementation is represented by the plan tickets below.
- A new upstream MC 26.x branch or upstream 1.21 branch maintenance is out of scope.
- Reintroducing official EMI, Create/Ponder/Flywheel, Embeddium/Oculus or GameStages is out of scope.
- Chapters-based GameStages replacement is out of scope until a separate compatibility decision is made.
- Feature parity with unverified optional dev/runtime mods is out of scope.
- Maintaining the 1.20.1 Forge line from the 26.1.2 branch is out of scope.
- Publishing to CurseForge or Modrinth beyond preparing correct metadata and artifacts is out of scope for the core port plan.
- MC 26.1.2 follow-up updates, MC 26.2 and later version ports are out of scope.

## Further Notes

- Domain vocabulary and resolved decisions live in [`CONTEXT.md`](../../CONTEXT.md).
- Research and decision evidence lives in the [wayfinding map](map.md) and its closed tickets: [dependency pins](issues/02-dep-pinning.md), [translation manual](issues/01-upstream-121-playbook.md), [API gap inventory](issues/05-api-gap-inventory.md), [MUI decision](issues/03-mui-config-decision.md), [cut scope](issues/04-cut-list-scope.md) and [sync workflow](issues/06-sync-workflow.md).
- The implementation task graph is listed in the map and described by tickets 08 through 17. Each ticket links back to this spec and states its own blocking relationship and acceptance criteria.
- The repository has no git metadata at the time this spec was written. The first implementation ticket must initialize or attach the repository to the upstream remote and record the baseline commit before any port commit is created.
- The repository AI policy requires disclosure of AI tooling and human understanding/review for any contribution, especially anything later proposed upstream.
