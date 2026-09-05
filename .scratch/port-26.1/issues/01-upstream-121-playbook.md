Type: research
Status: resolved
Blocked by: none

## Question

上游 1.20.1→1.21 分支之间做了哪些 Forge→NeoForge 迁移？输出一份 26.1 移植可复用的《翻译手册》。

## Notes

- 对比分支：`1.20.1` vs `1.21`（上游 [GregTechCEu/GregTech-Modern](https://github.com/GregTechCEu/GregTech-Modern)；1.21 = MC 1.21.1 + NeoForge 21.1.248）。
- 必看面：构建脚本与版本目录（注意 1.21 改用 `gradle/libs.versions.toml`）、包名与 import（`forge`→`neoforge`）、事件总线、registry / 注册、网络包、datagen、client 渲染、mixin、Registrate 与 MUI 在 1.21 分支用的版本（结果喂给「26.x 依赖版本钉死」与「MUI 与 Configuration 路线裁决」）。
- 只做 fact-finding，不做路线决策；发现直接记入本文件的 `## Findings`，不关闭 ticket。

## Findings

调查日期 2026-09-05，对比上游 `1.20.1` vs `1.21`（均处于 ~8.0.0）。先纠正一个前提：两分支都已用 NeoForged **ModDevGradle**（1.20.1 用 `net.neoforged.moddev.legacyforge`），所以迁移是 Forge→NeoForge **API** 迁移，不是构建插件换代。

### 1. 构建与版本目录

| 面 | 1.20.1 | 1.21 |
|---|---|---|
| MC / Loader | MC 1.20.1, Forge 47.4.0 | MC 1.21.1, NeoForge 21.1.248, loader `4` |
| MDG 插件 | `net.neoforged.moddev.legacyforge` 2.0.86 | `net.neoforged.moddev` 2.0.141 |
| Java | 17 | 21 |
| Parchment | 2023.09.03 | 2024.11.17 |
| settings 插件仓库 | `maven.minecraftforge.net` | `maven.neoforged.net` |
| 版本目录 | `gradle/libs.versions.toml` + `gradle/forge.versions.toml`（**1.21 沿用同名文件，别被名字误导**） | 同上结构 |

- `build.gradle`：1.21 删掉 `obfuscation { createRemappingConfiguration(...) }`（MDG 内置处理）；其余 sourceSets/configurations 结构基本不动。
- `dependencies.gradle` 前缀变化：`modCompileOnly/modLocalRuntime/modApi/modImplementation` → `compileOnly/localRuntime` + `jarJar(api(...))`；mixin 显式依赖（mixin 0.8.7、mixinExtras 0.5.0-rc.3、annotationProcessor）在 1.21 整段删除（NeoForge/MDG 自带）。
- `moddevgradle.gradle`：`legacyForge { version = minecraftForge }` → `neoForge { version = neoForge }`；`forge.enabledGameTestNamespaces` → `neoforge.enabledGameTestNamespaces`；`forge.logging.markers` 字面保留；1.21 新增 `validateAccessTransformers`、gameTestClient run、sodium shell-lib strip 任务。
- `resources.gradle` + `src/main/templates/META-INF/*.toml`：`loaderVersion [forge_major,)` → `[loader(4),)`，依赖 `mandatory=true/false` → `type="required"/"optional"`，`modId="forge"` → `modId="neoforge"` + `minecraft_version "[1.21, 1.21.1]"`；1.21 新增 `enumExtensions`、modernfix integration entrypoint、按 mod 条件加载的 `[[mixins]]` 块、sodium/embeddium/sable 可选依赖。

### 2. 包名/Import 翻译表

| 1.20.1 (Forge) | 1.21 (NeoForge) |
|---|---|
| `net.minecraftforge.eventbus.api.*` | `net.neoforged.bus.api.*` |
| `net.minecraftforge.fml.*`（Mod/ModList/FMLEnvironment/FMLLoader/ModLoader/lifecycle） | `net.neoforged.fml.*`（同名） |
| `net.minecraftforge.event.*`（AddPackFindersEvent 等） | `net.neoforged.neoforge.event.*` |
| `net.minecraftforge.common.capabilities.*` | `net.neoforged.neoforge.capabilities.*` |
| `net.minecraftforge.network.*` SimpleChannel | `net.neoforged.neoforge.network.*` payload 系统 |
| `net.minecraftforge.fluids.FluidStack` / energy / items | `net.neoforged.neoforge.fluids / energy / items` |
| `net.minecraftforge.client.event.*` | `net.neoforged.neoforge.client.event.*`（多为同名类） |
| `net.minecraftforge.server.ServerLifecycleHooks` | `net.neoforged.neoforge.server.ServerLifecycleHooks` |
| `net.minecraftforge.registries.*` | `net.neoforged.neoforge.registries.*`（RegisterEvent/NewRegistryEvent/DataPackRegistryEvent 同名） |
| `ResourceLocation.parse` | `tryParse` / `fromNamespaceAndPath` / `read` |

### 3. 入口与事件总线

- `FMLJavaModLoadingContext.get().getModEventBus()` → 构造器注入 `GTCEu(IEventBus modBus, FMLModContainer container)`，`GTCEu.gtModBus` 静态持有；`DistExecutor.unsafeRunForDist(ClientProxy::new, CommonProxy::new)` 删除，改为 `CommonProxy.init(modBus)` 直接调用 + `modBus.addListener(GTNetwork::registerPayloads)`；`@EventBusSubscriber(modid=...)` 保留（包换 Neo）。
- `FMLConstructModEvent` 延迟 init 模式取消；`isDataGen()` 由 `FMLLoader.getLaunchHandler().isData()` → `DatagenModLoader.isRunningDataGen()`。

### 4. 注册表

- 自定义 GT 注册表改用 `RegistryBuilder<>(key).sync(...).create()` + `NewRegistryEvent`（`GTRegistries.init(modBus)`，HIGHEST 解冻 / LOW 批量注册，`IdMappingEvent` 跟踪冻结）。
- 矿脉/基岩矿/基岩流体改为 **datapack registry**（`DataPackRegistryEvent.NewRegistry`），对应 7.0.0 breaking change「образи veins 必须 datagen」；旧版 vein 同步包 `SPacketSync{Ore,Fluid,BedrockOre}Veins` 删除。
- 材料流：`MaterialEvent`（ModLoader.postEvent）→ `RegisterEvent(Keys.MATERIAL)` + `PostMaterialEvent`（`ModLoader.postEventWrapContainerInModOrder`）。
- 香草注册表一律 `DeferredRegister.register(modBus)`（1.21 新增 `ATTACHMENT_TYPES`、`DATA_COMPONENTS`、`ITEM/FLUID_INGREDIENT_TYPES` 直挂 modBus；`GTGlobalLootModifiers` 入口消失，见下条 datagen）。

### 5. 网络

`SimpleChannel + INetPacket(FriendlyByteBuf encode/decode) + NetworkDirection + PacketDistributor` → `CustomPacketPayload（TYPE + StreamCodec CODEC + execute） + RegisterPayloadHandlersEvent（playToClient/playToServer/playBidirectional） + PacketDistributor.sendTo*`。发送端语义基本一一对应。

### 6. 能力/配方成分/物品数据

- Capability：Forge `CapabilityToken/RegisterCapabilitiesEvent` + `GTCapability.register` 集中式 → NeoForge `Capabilities.*` 逐 block/item `event.register*`（`RegisterCapabilitiesEvent` 同名异包）。
- NBT 成分 → Data Component：`StrictNBT/PartialNBT/NBTPredicateIngredient + CraftingHelper.register` → `DataComponentIngredient`、`Sized/Compound/Intersection/DataComponent` Fluid/Item Ingredient；datagen 新增 `ProviderType.DATA_MAP + DataMapsHandler`。
- `ProviderType.register` → `registerProvider`（Registrate API 改名）。

### 7. Client 渲染与 Mixin

- Mixin：`compatibilityLevel JAVA_17→JAVA_21`，mixinextras 显式依赖删除；client mixins 大换血——删 REI/Xaero/FTBChunks/Oculus/Embeddium 兼容及 `forge.*` accessor、`CapabilityDispatcher*`、`LootDataManagerMixin`；bloom 重写面向 **Sodium 0.8 NeoForge**（新增 `SectionCompiler/LightPipelineAware/BufferBuilderAccessor` 等，Embeddium→Sodium，附 forgified-Fabric-API 渲染依赖 + sodium shell-lib strip 任务）。
- Client 事件类多为同名换包（RegisterKeyMappings/RegisterGuiLayers/RegisterParticleProviders/EntityRenderersEvent/ModelEvent/RegisterItemDecorations/RegisterClientExtensions + `IClientFluidTypeExtensions`）。

### 8. 关键第三方版本（喂「依赖钉死」与「MUI/Configuration 裁决」）

| 库 | 1.20.1 | 1.21 |
|---|---|---|
| Registrate | `MC1.20-1.3.11` | `MC1.21-1.3.0+67` |
| Configuration | `3.1.0-forge`（`configuration-1.20.1`） | `3.1.1-neoforge`（`configuration-1.21.1`） |
| MUI | `3.3.1-SNAPSHOT`（`modularui-mc1.20.1`） | `3.3.1-SNAPSHOT`（`modularui-mc1.21.1`，坐标仅换 MC 后缀） |
| KJS/Rhino | 2001.6.5 / 2001.2.3（+architectury） | 2101.7.2 / 2101.2.7（architectury 依赖消失） |
| JEI / AE2 / Create / Curios | 15.20 / 15.0.18 / 6.0.6 / curios-forge 5.9.1 | 19.25 / 19.2.8 / 6.0.10 / curios-neoforge 9.4.2 |
| REI | 有（rei bundle） | **删除**（mods.toml 仅残留 rei 条件 mixin） |
| GameStages | 正常版本 | 无 1.21 版，用 Forge-1.20.3 占位（`TODO` 注释） |

26.1 移植启示：机械性替换（包名/总线/网络/DeferredRegister 挂载）可脚本化；真正的语义坑是 datapack 化矿脉、Data Component 化、Sodium 渲染重写三处。

## Answer

翻译手册已完成并记录在本票的 `## Findings`：上游 1.21 分支是 Forge→NeoForge API 迁移的参照，不是 26.x 基线；构建、事件总线、注册表、网络、数据组件、渲染和 mixin 的迁移边界均已列出。上游无 26.x 分支，因此最终实现仍从当前 1.20.1 基线移植，并把上游 1.21 作为迁移参考。
