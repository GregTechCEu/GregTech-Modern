package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.NoopIndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator.IndicatorPlacement;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.DikeVeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.cosmetics.GTCapes;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.GTOreVeins;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveWorkableMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.integration.kjs.builders.ElementBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.GTRecipeCategoryBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.GTRecipeTypeBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.WorldGenLayerBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.block.ActiveBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.block.CoilBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.*;
import com.gregtechceu.gtceu.integration.kjs.builders.material.*;
import com.gregtechceu.gtceu.integration.kjs.builders.worldgen.*;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;
import com.gregtechceu.gtceu.integration.kjs.helpers.MachineConstructors;
import com.gregtechceu.gtceu.integration.kjs.helpers.MachineModifiers;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTShapedRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.KJSHelpers;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.*;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import dev.latvian.mods.kubejs.registry.ServerRegistryRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;

public class GregTechKubeJSPlugin implements KubeJSPlugin {

    // spotless:off
    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.addDefault(GTRegistries.Keys.ELEMENT, ElementBuilder.class, ElementBuilder::new);
        registry.addDefault(GTRegistries.Keys.MATERIAL_ICON_SET, MaterialIconSetBuilder.class, MaterialIconSetBuilder::new);
        registry.addDefault(GTRegistries.Keys.MATERIAL, MaterialBuilderWrapper.class, MaterialBuilderWrapper::new);
        registry.of(GTRegistries.Keys.TAG_PREFIX, reg -> {
            reg.addDefault(TagPrefixBuilder.class, TagPrefixBuilder::new);
            reg.add(GTCEu.id("ore"), OreTagPrefixBuilder.class, OreTagPrefixBuilder::new);
        });
        registry.addDefault(GTRegistries.Keys.DIMENSION_MARKER, DimensionMarkerBuilder.class, DimensionMarkerBuilder::new);

        registry.addDefault(GTRegistries.Keys.RECIPE_TYPE, GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new);
        registry.of(Registries.RECIPE_TYPE, reg -> {
            reg.add(GTCEu.id("machine"), GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new);
        });
        registry.addDefault(GTRegistries.Keys.RECIPE_CATEGORY, GTRecipeCategoryBuilder.class, GTRecipeCategoryBuilder::new);

        registry.of(GTRegistries.Keys.MACHINE, reg -> {
            reg.addDefault(KJSTieredMachineBuilder.class, (id) -> new KJSTieredMachineBuilder(id, SimpleTieredMachine::new, false));

            reg.add(GTCEu.id("custom"), KJSTieredMachineBuilder.class, KJSTieredMachineBuilder::new);
            reg.add(GTCEu.id("steam"), KJSSteamMachineBuilder.class, KJSSteamMachineBuilder::new);
            reg.add(GTCEu.id("generator"), KJSTieredMachineBuilder.class, (id) -> new KJSTieredMachineBuilder(id, SimpleGeneratorMachine::new, true));

            reg.add(GTCEu.id("multiblock"), KJSMultiblockMachineBuilder.class, KJSMultiblockMachineBuilder::create);
            reg.add(GTCEu.id("tiered_multiblock"), KJSTieredMultiblockBuilder.class, KJSTieredMultiblockBuilder::new);
            reg.add(GTCEu.id("primitive"), KJSMultiblockMachineBuilder.class, (id) -> KJSMultiblockMachineBuilder.create(id, PrimitiveWorkableMachine::new));
        });

        registry.of(Registries.BLOCK, reg -> {
            reg.add(GTCEu.id("active"), ActiveBlockBuilder.class, ActiveBlockBuilder::new);
            reg.add(GTCEu.id("coil"), CoilBlockBuilder.class, CoilBlockBuilder::new);
        });

        registry.addDefault(GTRegistries.Keys.WORLD_GEN_LAYER, WorldGenLayerBuilder.class, WorldGenLayerBuilder::new);

        registry.addDefault(GTRegistries.Keys.ORE_VEIN, OreVeinDefinitionBuilder.class, OreVeinDefinitionBuilder::new);
        registry.addDefault(GTRegistries.Keys.BEDROCK_FLUID, BedrockFluidBuilder.class, BedrockFluidBuilder::new);
        registry.addDefault(GTRegistries.Keys.BEDROCK_ORE, BedrockOreBuilder.class, BedrockOreBuilder::new);
        registry.addDefault(GTRegistries.Keys.WORLD_GEN_LAYER, WorldGenLayerBuilder.class, WorldGenLayerBuilder::new);
    }

    @Override
    public void registerServerRegistries(ServerRegistryRegistry registry) {
        registry.register(GTRegistries.Keys.ORE_VEIN, GTOreDefinition.DIRECT_CODEC, GTOreDefinition.class);
        registry.register(GTRegistries.Keys.BEDROCK_FLUID, BedrockFluidDefinition.DIRECT_CODEC, BedrockFluidDefinition.class);
        registry.register(GTRegistries.Keys.BEDROCK_ORE, BedrockOreDefinition.DIRECT_CODEC, BedrockOreDefinition.class);
    }
    // spotless:on

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(GTCEuStartupEvents.GROUP);
        registry.register(GTCEuServerEvents.GROUP);
    }

    public static void generateMachineBlockModels() {
        RegistryObjectStorage.of(GTRegistries.Keys.MACHINE).forEach(builder -> {
            if (builder instanceof IMachineBuilderKJS machineBuilder) {
                try {
                    machineBuilder.generateMachineModels();
                } catch (IllegalStateException ignored) {}
            }
        });
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        // allow user to access all gtceu classes by importing them.
        filter.allow("com.gregtechceu.gtceu");
        filter.deny("com.gregtechceu.gtceu.core");
        filter.deny("com.gregtechceu.gtceu.common.network");
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry event) {
        for (var id : BuiltInRegistries.RECIPE_TYPE.keySet()) {
            RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(id);
            if (!(type instanceof GTRecipeType)) continue;
            event.register(id, GTRecipeSchema.SCHEMA);
        }
        event.namespace(GTCEu.MOD_ID).register("shaped", GTShapedRecipeSchema.SCHEMA);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(GTRecipeSchema.RECIPE_FACTORY);
        registry.register(GTShapedRecipeSchema.RECIPE_FACTORY);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.register(NbtTagComponent.NBT_TAG);
        registry.register(RecipeConditionComponent.RECIPE_CONDITION);
        registry.register(ResourceLocationComponent.RESOURCE_LOCATION);
        registry.register(RecipeCapabilityComponent.RECIPE_CAPABILITY);
        registry.register(GTRecipeComponents.CHANCE_LOGIC.type());
        registry.register(CapabilityMapComponent.CAPABILITY_MAP);

        registry.register(GTRecipeComponents.ITEM.type());
        registry.register(GTRecipeComponents.FLUID.type());
        registry.register(GTRecipeComponents.EU.type());
    }

    @Override
    public void registerBindings(BindingRegistry event) {
        // Mod related
        event.add("GTCEu", GTCEu.class);
        event.add("GTCEuAPI", GTCEuAPI.class);
        event.add("GTRegistries", GTRegistries.class);
        event.add("GTValues", GTValues.class);
        // Material related
        event.add("GTElements", GTElements.class);
        event.add("GTMaterials", GTMaterials.class);
        event.add("TagPrefix", TagPrefix.class);
        event.add("ItemGenerationCondition", TagPrefix.Conditions.class);
        event.add("MaterialEntry", MaterialEntry.class);
        event.add("GTMaterialFlags", MaterialFlags.class);
        event.add("GTFluidAttributes", FluidAttributes.class);
        event.add("GTFluidBuilder", FluidBuilder.class);
        event.add("GTFluidStorageKeys", FluidStorageKeys.class);
        event.add("GTFluidState", FluidState.class);
        event.add("GTMaterialIconSet", MaterialIconSet.class);
        event.add("GTMaterialIconType", MaterialIconType.class);
        event.add("ChemicalHelper", ChemicalHelper.class);
        event.add("PropertyKey", PropertyKey.class);
        event.add("ToolProperty", ToolProperty.class);
        event.add("ArmorProperty", ArmorProperty.class);
        event.add("GTToolType", GTToolType.class);
        // Block/Item related
        event.add("GTBlocks", GTBlocks.class);
        event.add("GTMaterialBlocks", GTMaterialBlocks.class);
        event.add("GCYMBlocks", GCYMBlocks.class);
        event.add("GTItems", GTItems.class);
        event.add("GTMaterialItems", GTMaterialItems.class);
        // Machine related
        event.add("GTMachines", GTMachines.class);
        event.add("GTMultiMachines", GTMultiMachines.class);
        event.add("GTMachineUtils", GTMachineUtils.class);
        event.add("GCYMMachines", GCYMMachines.class);
        // Multiblock related
        event.add("RotationState", RotationState.class);
        event.add("FactoryBlockPattern", MultiblockPatternBuilder.class);
        event.add("Predicates", Predicates.class);
        event.add("PartAbility", PartAbility.class);
        // Recipe related
        event.add("GTRecipeTypes", GTRecipeTypes.class);
        event.add("GTRecipeCategories", GTRecipeCategories.class);
        event.add("GTMedicalConditions", GTMedicalConditions.class);
        event.add("GTRecipeModifiers", GTRecipeModifiers.class);
        event.add("OverclockingLogic", OverclockingLogic.class);
        event.add("MachineConstructors", MachineConstructors.class);
        event.add("MachineModifiers", MachineModifiers.class);
        event.add("ModifierFunction", ModifierFunction.class);
        event.add("RecipeCapability", RecipeCapability.class);
        event.add("ChanceLogic", ChanceLogic.class);
        event.add("CleanroomType", CleanroomType.class);
        event.add("CraftingComponent", CraftingComponent.class);
        event.add("GTCraftingComponents", GTCraftingComponents.class);
        event.add("EnergyStack", EnergyStack.class);
        event.add("IOEnergyStack", EnergyStack.WithIO.class);
        // event.add("NBTPredicates", NBTPredicates.class);
        // Sound related
        event.add("GTSoundEntries", GTSoundEntries.class);
        event.add("SoundType", SoundType.class);
        // Client/Server data related
        event.add("GTModels", GTModels.class);
        event.add("GTMachineModels", GTMachineModels.class);
        event.add("GTModelProperties", GTMachineModelProperties.class);
        event.add("GTDynamicRenders", DynamicRenderHelper.class);
        event.add("GTGuiTextures", GTGuiTextures.class);
        event.add("IO", IO.class);

        // Hazard Related
        event.add("HazardProperty", HazardProperty.class);
        event.add("MedicalCondition", MedicalCondition.class);
        event.add("Symptom", Symptom.class);
        // World Gen Related
        event.add("GTOreVein", GTOreDefinition.class);
        event.add("OreVeinDefinition", GTOreDefinition.class);
        event.add("GTLayerPattern", GTLayerPattern.class);
        event.add("GTDikeBlockDefinition", DikeVeinGenerator.DikeBlockDefinition.class);
        event.add("GTOres", GTOreVeins.class);
        event.add("GTOreVeins", GTOreVeins.class);
        event.add("GTWorldGenLayers", WorldGenLayers.class);
        // Cape related
        event.add("GTCapes", GTCapes.class);
        event.add("CapeRegistry", CapeRegistry.class);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        registry.register(GTResourceLocation.class, GTResourceLocation::wrap);

        registry.register(MaterialEntry.class, MaterialEntry::of);
        registry.register(MaterialStack.class, o -> {
            if (o instanceof MaterialStack stack) return stack;
            if (o instanceof Material material) return new MaterialStack(material, 1);
            if (o instanceof CharSequence chars) return MaterialStack.fromString(chars);
            return null;
        });
        registry.register(MaterialStackWrapper.class, o -> {
            if (o instanceof MaterialStackWrapper wrapper) return wrapper;
            if (o instanceof MaterialStack stack) return new MaterialStackWrapper(stack::material, stack.amount());
            if (o instanceof Material material) return new MaterialStackWrapper(() -> material, 1);
            if (o instanceof CharSequence chars) return MaterialStackWrapper.fromString(chars);
            return null;
        });

        registry.registerMapCodec(HeightRangePlacement.class, HeightRangePlacement.CODEC);
        registry.registerCodec(BiomeWeightModifier.class, BiomeWeightModifier.CODEC, BiomeWeightModifier.EMPTY);
        registry.registerCodec(VeinGenerator.class, VeinGenerator.DIRECT_CODEC, NoopVeinGenerator.INSTANCE);
        registry.registerCodec(IndicatorGenerator.class, IndicatorGenerator.DIRECT_CODEC,
                NoopIndicatorGenerator.INSTANCE);
        registry.registerCodec(IndicatorPlacement.class, IndicatorPlacement.CODEC, IndicatorPlacement.SURFACE);

        registry.register(IWorldGenLayer.RuleTestSupplier.class, (cx, o, t) -> {
            if (o instanceof IWorldGenLayer.RuleTestSupplier supplier) return supplier;
            if (o instanceof CharSequence) {
                return () -> BlockStatePredicate.wrap(cx, o).asRuleTest();
            }
            return () -> BlockStatePredicate.wrapRuleTest(cx, o);
        });

        registry.register(CraftingComponent.class, o -> {
            if (o instanceof CraftingComponent comp) return comp;
            if (o instanceof CharSequence str) return CraftingComponent.ALL_COMPONENTS.get(str.toString());
            return null;
        });
        registry.register(EnergyStack.class, KJSHelpers::parseEnergyStack);
        registry.register(EnergyStack.WithIO.class, KJSHelpers::parseIOEnergyStack);
    }
}
