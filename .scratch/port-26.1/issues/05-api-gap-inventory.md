Type: research
Status: resolved
Blocked by: none

## Question

MC 1.20.1→26.1.2 与 Forge→NeoForge 的 API 差异中，GTM 体量（1725 个 java 文件）会撞上哪些？输出差异清单。

## Notes

- 必查面：Mojang mappings 与方法签名变更、registry / 注册与 datagen、事件总线、网络（networking）、client 渲染管线、mixin 目标方法、配置文件库。
- 可与「上游 1.21 翻译手册」并行先行：重点放在 1.21→26.x 的增量差异 + Mojang 大版本变更，不要重复造 01 的 Forge→NeoForge 部分，01 完成后做一次对齐。
- 只做 fact-finding；发现记入 `## Findings`，不关闭 ticket。

## Findings

> 范围：1.21→26.x 增量 + Mojang 大版本变更；Forge→NeoForge 基础翻译见 01 票，本票不重复。来源：NeoForge 26.1 primer / 1.21.x primers / 21.x release notes / NeoForge docs（networking、registries、transfer rework）。

- [ ] 工具链/mappings（blocks-build）：Java 21→25（Gradle ≥9.1，MDG ≥2.0.141，IDE 2025.2+/2025-12），删除 Parchment（26.1 已反混淆，用 Mojang 官方参数名）；`Block/Item.Properties` 必须显式设 id（用 `DeferredRegister.Blocks/Items.register*` 自动处理）；`Level#random` 改 protected→`getRandom()`（mechanical-rename，全仓量大）。
- [ ] 注册表/数据驱动（blocks-build 为主）：`DeferredRegister` + `RegistryBuilder.sync(true)` 仍在；`RegistryAccess.registry→lookup`、`Registry.getHolder→get` 改名（mechanical-rename）；`Holder/HolderSet` 普及（`HolderSet.direct`、`wrapAsHolder`、`getOrThrow`），tag 目录去复数（`tags/blocks→tags/block` 等，mechanical-rename）；附魔已是 datapack registry（`Holder<Enchantment>`，behavior-change，GTM 附魔相关配方/Tooltip 必改）。
- [ ] Datagen（blocks-build）：`GatherDataEvent` 在 1.21.4+ 拆分为 Client/Server（`clientData`/`serverData` run），`RecipeProvider`/`DatapackBuiltinEntriesProvider + RegistrySetBuilder` 用法变，`event.createProvider/createDatapackRegistryObjects` 新 API；26.1 战利品系统 unrolling（`LootPoolEntryType/FunctionType/ConditionType…` 删除→直接注册 `MapCodec`，`getType→codec`，`CODEC→MAP_CODEC`）+ `Validatable/ValidationContext` 校验重构（behavior-change，GTM 战利品/矿脉战利品表受影响）；村民交易 datapack 化（GTM 基本无感）。
- [ ] DataMap（behavior-change）：`COMPOSTABLES`、`VIBRATION_FREQUENCY` 等静态 map 改 DataMap（`DataMapType/AdvancedDataMapType + DataMapProvider`），Holder 查询；GTM 材料/方块附加属性若用静态 map 需迁移。
- [ ] 事件总线（mechanical-rename + 部分 blocks-build）：`Event.Result` 删除、TickEvent 重构（见 01 票增量）；新增 `RegisterPayloadHandlersEvent` / `RegisterClientPayloadHandlersEvent`（网络）、`RegisterCapabilitiesEvent` key 变更、`ExtractLevelRenderStateEvent`（渲染）；`RenderHighlightEvent` 删除→`ExtractBlockOutlineRenderStateEvent + CustomBlockOutlineRenderer`。
- [ ] 网络（blocks-build，重写面最大）：Forge `SimpleChannel/EventChannel + FriendlyByteBuf.Reader` 删除→`CustomPacketPayload + Type<T> + StreamCodec + PayloadRegistrar`（`playBidirectional/playToClient/playToServer`，`executesOn(HandlerThread)`），发送经 `PacketDistributor` / `ClientPacketDistributor.sendToServer`，上限 32KiB（C→S）/1MiB（S→C），`versioned/optional` 协商；1.21.6 起 client handler 必须经 `RegisterClientPayloadHandlersEvent` 注册（`DirectionalPayloadHandler` 删除）。GTM 大量机器 GUI 同步包需逐个重写 codec。
- [ ] 传输/Capability（behavior-change，工作量最大）：21.9.1 transfer rework：`IItemHandler/IFluidHandler/IEnergyStorage`→`ResourceHandler<T> + EnergyHandler + SnapshotJournal` 事务模型 + `ItemAccess` 上下文，`Capabilities.ItemHandler.*→Capabilities.Item.*`，旧接口 deprecated 但可用适配器（`IItemHandler.of` 等）分阶段迁移。GTM 机器仓/管道/线缆/ covers 全撞上。
- [ ] 客户端渲染（behavior-change，重写面第二）：1.21.2 实体 `EntityRenderState`（`EntityRenderer<T,S>` + `createRenderState/extractRenderState`，`RenderLayer<T,S>`）；1.21.4 物品模型 datagen 化（`BlockEntityWithoutLevelRenderer` 删除→`SpecialModelRenderer`，`ItemModelResolver/ItemStackRenderState`）；1.21.6 GUI 两阶段（`GuiGraphics` 持 `GuiRenderState`，`submit*`，tooltip `set*ForNextFrame`，颜色须 ARGB 不透明，`RenderPipeline` 排序）；1.21.9+ 世界渲染两阶段（`BlockEntityRenderer<T,S>` 三方法 + `SubmitNodeCollector`，`RenderLevelStageEvent` 需经 `LevelRenderState(BaseRenderState)` 挂数据，粒子 `FeatureRenderDispatcher`）。GTM 大量 Screen/BER/动态高亮需重测；shader JSON 与盔甲 `ArmorMaterial→ToolMaterial/Equippable/EquipmentModel` 改动连带。
- [ ] Mixin 目标（blocks-build + 脆弱）：高频目标签名变：`BlockBehaviour#onRemove`→`preRemoveSideEffects + affectNeighborsAfterRemoval`，`Item#getEnchantmentValue/isValidRepairItem` 删除（→data component），`Tier→ToolMaterial`，`EntityEquipment` 重构，模型烘焙 `getDependencies/resolveParents→resolveDependencies`；另 NeoForge 已切 Fabric Mixin（refmap/config 差异，见 01 票），JEP 447（super 前语句）影响构造注入写法。建议列 mixin 目标清单逐个对 vanilla 用法 diff。
- [ ] 配置库（mechanical-rename，低风险）：仍 NightConfig 3.8.x + `ModConfigSpec` + `registerConfig`（CLIENT/COMMON/SERVER 语义与同步不变）；21.0 清理 breaking（`UnmodifiableConfigWrapper` 解耦、`defineList` 新增 GUI 默认值参数、`IConfigScreenFactory` 签名变）+ 内置 `ConfigurationScreen` 可选接入。GTM 自有 config 预计机械改动。

## Answer

API 差异盘点已完成并记录在本票的 `## Findings`。最终 spec 必须把网络、传输/Capability、客户端渲染列为行为级重写模块，把工具链、注册表、datagen、DataMap、事件、mixin 和配置列为机械改名或局部行为变更模块，并为每类模块补充文件范围与验收标准。
