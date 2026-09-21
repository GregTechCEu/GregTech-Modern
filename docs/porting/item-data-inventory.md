# Item-data migration inventory

Baseline: `b0d5fa0d0`, Minecraft 26.2 / NeoForge 26.2.0.88. AI-assisted with OpenAI Codex.

This is a lexical inventory of candidate usages, including related tool/part accessors. It is not a claim that every match is ItemStack NBT. Registry tags (`TagPrefix`, `TagType`, `MaterialToolTier`, `TagValueAccessor`), fluid tags, block/entity save compounds and recipe payloads require separate classification. Do not replace those blindly with item components.

Search: `getOrCreateTag`, `getTag`, `hasTag`, `setTag`, `getOrCreateTagElement`, `getTagElement`, `removeTagKey`, `getToolTag`, `getBehaviorsTag`, `getOrCreatePartStatsTag` followed by `(` in `src/main/java`.

351 matching lines in 110 files. Line numbers refer to the baseline above. See [item-components.md](item-components.md) for the migrated slice, boundaries and interfaces.

| File | Matching lines |
| --- | --- |
| `src/main/java/com/gregtechceu/gtceu/api/block/MetaMachineBlock.java` | 123, 187, 242, 245 |
| `src/main/java/com/gregtechceu/gtceu/api/capability/recipe/ItemRecipeCapability.java` | 400 |
| `src/main/java/com/gregtechceu/gtceu/api/cover/filter/CompositeFilter.java` | 45 |
| `src/main/java/com/gregtechceu/gtceu/api/cover/filter/Filter.java` | 60 |
| `src/main/java/com/gregtechceu/gtceu/api/cover/filter/SimpleFluidFilter.java` | 50 |
| `src/main/java/com/gregtechceu/gtceu/api/cover/filter/SimpleItemFilter.java` | 51 |
| `src/main/java/com/gregtechceu/gtceu/api/cover/filter/SmartItemFilter.java` | 49 |
| `src/main/java/com/gregtechceu/gtceu/api/cover/filter/TagFilter.java` | 46 |
| `src/main/java/com/gregtechceu/gtceu/api/data/chemical/ChemicalHelper.java` | 323, 332 |
| `src/main/java/com/gregtechceu/gtceu/api/data/tag/TagPrefix.java` | 1221, 1229, 1236, 1244, 1251 |
| `src/main/java/com/gregtechceu/gtceu/api/data/tag/TagType.java` | 80 |
| `src/main/java/com/gregtechceu/gtceu/api/data/worldgen/bedrockfluid/BedrockFluidDefinition.java` | 186 |
| `src/main/java/com/gregtechceu/gtceu/api/data/worldgen/bedrockore/BedrockOreDefinition.java` | 201 |
| `src/main/java/com/gregtechceu/gtceu/api/item/capability/ElectricItem.java` | 29, 33, 43, 52, 61 |
| `src/main/java/com/gregtechceu/gtceu/api/item/component/ElectricStats.java` | 167, 173, 238 |
| `src/main/java/com/gregtechceu/gtceu/api/item/component/IMaterialPartItem.java` | 31, 34, 35, 55, 68 |
| `src/main/java/com/gregtechceu/gtceu/api/item/component/ThermalFluidStats.java` | 76 |
| `src/main/java/com/gregtechceu/gtceu/api/item/IGTTool.java` | 116, 123, 126, 160, 237, 247, 256, 267, 282, 292, 311, 321, 371, 444, 512, 513, 532, 575, 696, 756, 847, 862, 893, 897, 981, 982 |
| `src/main/java/com/gregtechceu/gtceu/api/item/tool/MaterialToolTier.java` | 62 |
| `src/main/java/com/gregtechceu/gtceu/api/item/tool/ToolHelper.java` | 158, 159, 162, 163, 167, 202, 265, 266, 334, 338, 726 |
| `src/main/java/com/gregtechceu/gtceu/api/item/tool/TreeFellingHelper.java` | 94 |
| `src/main/java/com/gregtechceu/gtceu/api/misc/forge/QuantumFluidHandlerItemStack.java` | 40, 52, 63, 160, 161 |
| `src/main/java/com/gregtechceu/gtceu/api/multiblock/Predicates.java` | 153 |
| `src/main/java/com/gregtechceu/gtceu/api/multiblock/predicates/PredicateBuilder.java` | 153, 160 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/ingredient/FluidContainerIngredient.java` | 50 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/ingredient/FluidIngredient.java` | 110, 225, 238 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/ingredient/IntProviderIngredient.java` | 88 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/ingredient/NBTPredicateIngredient.java` | 55, 72, 73 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/ingredient/SizedIngredient.java` | 62, 120 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/lookup/ingredient/item/ItemTagMapIngredient.java` | 30 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/lookup/ingredient/item/StrictNBTItemStackMapIngredient.java` | 37 |
| `src/main/java/com/gregtechceu/gtceu/api/recipe/ShapedEnergyTransferRecipe.java` | 61, 62, 81, 82 |
| `src/main/java/com/gregtechceu/gtceu/api/sync_system/managed/ManagedSyncBlockEntity.java` | 82 |
| `src/main/java/com/gregtechceu/gtceu/client/renderer/item/decorator/GTLampItemOverlayRenderer.java` | 34, 38 |
| `src/main/java/com/gregtechceu/gtceu/client/renderer/item/ToolChargeBarRenderer.java` | 54 |
| `src/main/java/com/gregtechceu/gtceu/client/renderer/machine/impl/QuantumChestItemRender.java` | 53, 57, 58 |
| `src/main/java/com/gregtechceu/gtceu/client/renderer/machine/impl/QuantumTankFluidRender.java` | 58, 61, 62 |
| `src/main/java/com/gregtechceu/gtceu/common/block/LampBlock.java` | 93, 145, 152, 153, 167 |
| `src/main/java/com/gregtechceu/gtceu/common/CommonEventListener.java` | 428 |
| `src/main/java/com/gregtechceu/gtceu/common/cover/ComputerMonitorCover.java` | 186, 204 |
| `src/main/java/com/gregtechceu/gtceu/common/cover/WirelessTransmitterCover.java` | 44, 45, 46, 47, 48 |
| `src/main/java/com/gregtechceu/gtceu/common/data/GTItems.java` | 444, 456, 2202 |
| `src/main/java/com/gregtechceu/gtceu/common/data/GTMachines.java` | 614, 615, 616, 632, 633, 634 |
| `src/main/java/com/gregtechceu/gtceu/common/data/GTPlaceholders.java` | 559, 561, 570, 573, 577, 580, 614, 740, 741, 743, 827, 829, 831, 838, 952, 953 |
| `src/main/java/com/gregtechceu/gtceu/common/data/machines/GTMachineUtils.java` | 778, 779, 780, 781, 789, 790, 791 |
| `src/main/java/com/gregtechceu/gtceu/common/fluid/potion/BottleItemFluidHandler.java` | 22 |
| `src/main/java/com/gregtechceu/gtceu/common/fluid/potion/PotionFluid.java` | 63, 71, 118, 126 |
| `src/main/java/com/gregtechceu/gtceu/common/fluid/potion/PotionFluidHelper.java` | 63, 94, 97, 100, 110 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/AdvancedNanoMuscleSuite.java` | 52, 148, 172, 205 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/AdvancedQuarkTechSuite.java` | 52, 154, 177, 210 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/IJetpack.java` | 172 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/Jetpack.java` | 51, 133, 156 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/NanoMuscleSuite.java` | 54, 181 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/NightvisionGoggles.java` | 39, 97 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/PowerlessJetpack.java` | 66, 139, 264 |
| `src/main/java/com/gregtechceu/gtceu/common/item/armor/QuarkTechSuite.java` | 75, 357, 373 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/ColorSprayBehaviour.java` | 460, 467 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/ConsumedBehaviour.java` | 36, 43, 46 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/DataItemBehavior.java` | 54, 57, 58, 72, 73, 76, 79, 80, 81, 82, 83, 85, 88, 92, 96, 97 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/FacadeItemBehaviour.java` | 58, 103, 115 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/IntCircuitBehaviour.java` | 45, 51, 60, 63 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/ItemMagnetBehavior.java` | 83, 90, 95, 96, 117, 119, 209, 222, 265, 385, 387 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/LighterBehavior.java` | 78, 81, 88, 139, 177, 205 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/MachineConfigCopyBehaviour.java` | 76, 88, 96, 215 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/PortableScannerBehavior.java` | 142, 151 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/ProspectorScannerBehavior.java` | 59, 67 |
| `src/main/java/com/gregtechceu/gtceu/common/item/behavior/ToggleEnergyConsumerBehavior.java` | 70, 75 |
| `src/main/java/com/gregtechceu/gtceu/common/item/LampBlockItem.java` | 46, 49 |
| `src/main/java/com/gregtechceu/gtceu/common/item/modules/ImageModuleBehaviour.java` | 59, 63 |
| `src/main/java/com/gregtechceu/gtceu/common/item/modules/TextModuleBehaviour.java` | 34, 35, 45, 51, 95, 99, 101, 105, 109, 113, 114, 121, 126 |
| `src/main/java/com/gregtechceu/gtceu/common/item/tool/behavior/AOEConfigUIBehavior.java` | 38, 70, 73, 75, 86 |
| `src/main/java/com/gregtechceu/gtceu/common/item/tool/behavior/ShearBehavior.java` | 157 |
| `src/main/java/com/gregtechceu/gtceu/common/item/tool/behavior/ToolModeSwitchBehavior.java` | 45, 95, 113 |
| `src/main/java/com/gregtechceu/gtceu/common/item/tool/behavior/TorchPlaceBehavior.java` | 40 |
| `src/main/java/com/gregtechceu/gtceu/common/item/tool/behavior/TreeFellingBehavior.java` | 62 |
| `src/main/java/com/gregtechceu/gtceu/common/item/tool/ToolEventHandlers.java` | 114, 123, 188 |
| `src/main/java/com/gregtechceu/gtceu/common/machine/multiblock/electric/monitor/MonitorGroup.java` | 159, 182 |
| `src/main/java/com/gregtechceu/gtceu/common/machine/trait/AutoOutputTrait.java` | 349 |
| `src/main/java/com/gregtechceu/gtceu/common/machine/trait/customlogic/BreweryLogic.java` | 220 |
| `src/main/java/com/gregtechceu/gtceu/core/mixins/ItemStackMixin.java` | 101 |
| `src/main/java/com/gregtechceu/gtceu/core/mixins/SmithingTransformRecipeMixin.java` | 21, 27, 31 |
| `src/main/java/com/gregtechceu/gtceu/core/mixins/TagValueAccessor.java` | 14 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder.java` | 452, 974, 992 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/builder/ShapedEnergyTransferRecipeBuilder.java` | 65, 105, 170, 171 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/builder/ShapedRecipeBuilder.java` | 64, 89, 159, 160 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/builder/ShapelessRecipeBuilder.java` | 52, 79, 107, 108 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/builder/SimpleCookingRecipeBuilder.java` | 70, 118, 119 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/generated/OreRecipeHandler.java` | 66, 215 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler.java` | 386, 388 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/misc/CustomToolRecipes.java` | 224, 267 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/misc/RecyclingRecipes.java` | 147, 233 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/serialized/chemistry/ReactorRecipes.java` | 645 |
| `src/main/java/com/gregtechceu/gtceu/data/recipe/VanillaRecipeHelper.java` | 375, 475, 547, 617 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/gui/AEConfigWidget.java` | 405 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/machine/MEInputBusPartMachine.java` | 180, 210, 219 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/machine/MEInputHatchPartMachine.java` | 196, 205 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/machine/MEOutputHatchPartMachine.java` | 250 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine.java` | 885 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferProxyPartMachine.java` | 126, 127, 128, 129 |
| `src/main/java/com/gregtechceu/gtceu/integration/ae2/utils/AEUtil.java` | 21, 56 |
| `src/main/java/com/gregtechceu/gtceu/integration/kjs/builders/FluidVeinBuilderJS.java` | 65, 79 |
| `src/main/java/com/gregtechceu/gtceu/integration/kjs/events/CraftingComponentsEventJS.java` | 54 |
| `src/main/java/com/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema.java` | 303, 777, 1309, 1326 |
| `src/main/java/com/gregtechceu/gtceu/integration/kjs/recipe/KJSHelpers.java` | 126 |
| `src/main/java/com/gregtechceu/gtceu/integration/recipeviewer/emi/GTEMIPlugin.java` | 86 |
| `src/main/java/com/gregtechceu/gtceu/integration/recipeviewer/jei/subtype/PotionFluidSubtypeInterpreter.java` | 19, 22 |
| `src/main/java/com/gregtechceu/gtceu/utils/GTUtil.java` | 592, 593, 605, 606, 607, 658 |
| `src/main/java/com/gregtechceu/gtceu/utils/IngredientEquality.java` | 32, 148 |
| `src/main/java/com/gregtechceu/gtceu/utils/ItemStackHashStrategy.java` | 106, 116 |
| `src/main/java/com/gregtechceu/gtceu/utils/ResearchManager.java` | 74, 102, 140, 204, 212 |
