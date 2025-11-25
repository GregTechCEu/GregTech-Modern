package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator.IndicatorPlacement;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.DikeVeinGenerator;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.DummyCraftingContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.cosmetics.GTCapes;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveFancyUIWorkableMachine;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;
import com.gregtechceu.gtceu.integration.kjs.builders.*;
import com.gregtechceu.gtceu.integration.kjs.builders.block.ActiveBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.block.CoilBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.*;
import com.gregtechceu.gtceu.integration.kjs.builders.prefix.BasicTagPrefixBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.prefix.OreTagPrefixBuilder;
import com.gregtechceu.gtceu.integration.kjs.helpers.MachineConstructors;
import com.gregtechceu.gtceu.integration.kjs.helpers.MachineModifiers;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTShapedRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.KJSHelpers;
import com.gregtechceu.gtceu.integration.kjs.recipe.WrappingRecipeSchemaType;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ExtendedOutputItem;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockStateProvider;

import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.recipe.KubeJSRecipeEventHandler;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.KEY;
import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.PATTERN;
import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.RESULT;

public class GregTechKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void initStartup() {
        super.initStartup();
    }

    @Override
    public void init() {
        super.init();
        GTRegistryInfo.ELEMENT.addType("basic", ElementBuilder.class, ElementBuilder::new, true);

        GTRegistryInfo.MATERIAL_ICON_SET.addType("basic", MaterialIconSetBuilder.class, MaterialIconSetBuilder::new,
                true);
        GTRegistryInfo.MATERIAL_ICON_TYPE.addType("basic", MaterialIconTypeBuilder.class, MaterialIconTypeBuilder::new,
                true);

        GTRegistryInfo.MATERIAL.addType("basic", Material.Builder.class, Material.Builder::new, true);

        GTRegistryInfo.RECIPE_TYPE.addType("basic", GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new, true);
        GTRegistryInfo.RECIPE_CATEGORY.addType("basic", GTRecipeCategoryBuilder.class, GTRecipeCategoryBuilder::new,
                true);

        GTRegistryInfo.MACHINE.addType("simple", KJSWrappingMachineBuilder.class,
                (id) -> new KJSWrappingMachineBuilder(id,
                        new KJSTieredMachineBuilder(id, SimpleTieredMachine::new,
                                SimpleTieredMachine.EDITABLE_UI_CREATOR, false)),
                true);
        GTRegistryInfo.MACHINE.addType("custom", KJSWrappingMachineBuilder.class,
                (id) -> new KJSWrappingMachineBuilder(id, new KJSTieredMachineBuilder(id)),
                false);
        GTRegistryInfo.MACHINE.addType("steam", KJSSteamMachineBuilder.class,
                KJSSteamMachineBuilder::new, false);
        GTRegistryInfo.MACHINE.addType("generator", KJSWrappingMachineBuilder.class,
                (id) -> new KJSWrappingMachineBuilder(id,
                        new KJSTieredMachineBuilder(id, SimpleGeneratorMachine::new,
                                SimpleGeneratorMachine.EDITABLE_UI_CREATOR, true)),
                false);
        GTRegistryInfo.MACHINE.addType("multiblock", MultiblockMachineBuilder.class,
                KJSWrappingMultiblockBuilder::createKJSMulti, false);
        GTRegistryInfo.MACHINE.addType("tiered_multiblock", KJSWrappingMultiblockBuilder.class,
                (id) -> new KJSWrappingMultiblockBuilder(id, new KJSTieredMultiblockBuilder(id)), false);
        GTRegistryInfo.MACHINE.addType("primitive", MultiblockMachineBuilder.class,
                (id) -> KJSWrappingMultiblockBuilder.createKJSMulti(id, PrimitiveFancyUIWorkableMachine::new),
                false);

        GTRegistryInfo.WORLD_GEN_LAYER.addType("basic", WorldGenLayerBuilder.class, WorldGenLayerBuilder::new, true);

        GTRegistryInfo.TAG_PREFIX.addType("basic", BasicTagPrefixBuilder.class, BasicTagPrefixBuilder::new, true);
        GTRegistryInfo.TAG_PREFIX.addType("ore", OreTagPrefixBuilder.class, OreTagPrefixBuilder::new, false);

        GTRegistryInfo.DIMENSION_MARKER.addType("basic", DimensionMarker.Builder.class, DimensionMarker.Builder::new,
                true);

        RegistryInfo.BLOCK.addType("gtceu:active", ActiveBlockBuilder.class, ActiveBlockBuilder::new);
        RegistryInfo.BLOCK.addType("gtceu:coil", CoilBlockBuilder.class, CoilBlockBuilder::new);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        GTCEuStartupEvents.GROUP.register();
        GTCEuServerEvents.GROUP.register();
    }

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        GTRegistryInfo.ALL_BUILDERS.forEach(builderBase -> builderBase.generateDataJsons(generator));
    }

    // Fake a data provider for the GT model builders so we don't need to handle this ourselves in any way :3
    public static RuntimeBlockStateProvider RUNTIME_BLOCKSTATE_PROVIDER = new RuntimeBlockStateProvider(
            GTRegistration.REGISTRATE, new PackOutput(KubeJSPaths.DIRECTORY),
            (loc, json) -> {
                if (!loc.getPath().endsWith(".json")) {
                    loc = loc.withSuffix(".json");
                }
                GTDynamicResourcePack.addResource(loc, json);
            });

    public static void generateMachineBlockModels() {
        GTRegistryInfo.ALL_BUILDERS.forEach(builderBase -> {
            try {
                builderBase.generateAssetJsons(null);
            } catch (IllegalStateException ignored) {}
        });
        GregTechKubeJSPlugin.RUNTIME_BLOCKSTATE_PROVIDER.run();
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        GTRegistryInfo.ALL_BUILDERS.forEach(builderBase -> builderBase.generateAssetJsons(generator));
    }

    @Override
    public void generateLang(LangEventJS event) {
        GTRegistryInfo.ALL_BUILDERS.forEach(builderBase -> builderBase.generateLang(event));
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
        // allow user to access all gtceu classes by importing them.
        filter.allow("com.gregtechceu.gtceu");
        filter.deny("com.gregtechceu.gtceu.core");
        filter.deny("com.gregtechceu.gtceu.common.network");
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        super.registerRecipeSchemas(event);

        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), GTRecipeSchema.SCHEMA);
        }
        var ns = event.namespace(GTCEu.MOD_ID);
        ns.put("shaped", new WrappingRecipeSchemaType(ns, GTCEu.id("shaped"),
                GTShapedRecipeSchema.SCHEMA, KubeJSRecipeEventHandler.SHAPED.get()));
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistryEvent event) {
        event.register("compoundTag", GTRecipeComponents.TAG);
        event.register("recipeCondition", GTRecipeComponents.RECIPE_CONDITION);
        event.register("resourceLocation", GTRecipeComponents.RESOURCE_LOCATION);
        event.register("recipeCapability", GTRecipeComponents.RECIPE_CAPABILITY);
        event.register("chanceLogic", GTRecipeComponents.CHANCE_LOGIC);
        event.register("gtRecipeInputs", GTRecipeComponents.IN);
        event.register("gtRecipeTickInputs", GTRecipeComponents.TICK_IN);
        event.register("gtRecipeOutputs", GTRecipeComponents.OUT);
        event.register("gtRecipeTickOutputs", GTRecipeComponents.TICK_OUT);

        event.register("gtItemIn", GTRecipeComponents.ITEM_IN);
        event.register("gtItemOut", GTRecipeComponents.ITEM_OUT);
        event.register("gtFluidIn", GTRecipeComponents.FLUID_IN);
        event.register("gtFluidOut", GTRecipeComponents.FLUID_OUT);
        event.register("gtEuIn", GTRecipeComponents.EU_IN);
        event.register("gtEuOut", GTRecipeComponents.EU_OUT);
        event.register("gtCwuIn", GTRecipeComponents.CWU_IN);
        event.register("gtCwuOut", GTRecipeComponents.CWU_OUT);

        event.register("gtChance", GTRecipeComponents.CHANCE_LOGIC_MAP);
        event.register("extendedOutputItem", GTRecipeComponents.EXTENDED_OUTPUT);

        event.register("fluidIngredient", GTRecipeComponents.FLUID_INGREDIENT);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        // Mod related
        event.add("GTCEu", GTCEu.class);
        event.add("GTCEuAPI", GTCEuAPI.class);
        event.add("GTRegistries", GTRegistries.class);
        event.add("GTValues", GTValues.class);
        // Material related
        event.add("GTElements", GTElements.class);
        event.add("GTMaterials", GTMaterials.class);
        event.add("GTMaterialRegistry", MaterialRegistryManager.getInstance());
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
        event.add("FactoryBlockPattern", FactoryBlockPattern.class);
        event.add("MultiblockShapeInfo", MultiblockShapeInfo.class);
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
        event.add("NBTPredicates", NBTPredicates.class);
        // Sound related
        event.add("GTSoundEntries", GTSoundEntries.class);
        event.add("SoundType", SoundType.class);
        // GUI related
        event.add("GuiTextures", GuiTextures.class);
        // Client/Server data related
        event.add("GTModels", GTModels.class);
        event.add("GTMachineModels", GTMachineModels.class);
        event.add("GTModelProperties", GTMachineModelProperties.class);
        event.add("GTDynamicRenders", DynamicRenderHelper.class);

        // Hazard Related
        event.add("HazardProperty", HazardProperty.class);
        event.add("MedicalCondition", MedicalCondition.class);
        event.add("Symptom", Symptom.class);
        // World Gen Related
        event.add("GTOreVein", GTOreDefinition.class);
        event.add("GTLayerPattern", GTLayerPattern.class);
        event.add("GTDikeBlockDefinition", DikeVeinGenerator.DikeBlockDefinition.class);
        event.add("GTOres", GTOres.class);
        event.add("GTWorldGenLayers", WorldGenLayers.class);
        // Cape related
        event.add("GTCapes", GTCapes.class);
        event.add("CapeRegistry", CapeRegistry.class);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        super.registerTypeWrappers(type, typeWrappers);
        typeWrappers.registerSimple(GTRecipeType.class, o -> {
            if (o instanceof Wrapper w) {
                o = w.unwrap();
            }
            if (o instanceof GTRecipeType recipeType) return recipeType;
            if (o instanceof CharSequence chars) return GTRecipeTypes.get(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(GTRecipeCategory.class, o -> {
            if (o instanceof Wrapper w) {
                o = w.unwrap();
            }
            if (o instanceof GTRecipeCategory recipeCategory) return recipeCategory;
            if (o instanceof CharSequence chars) return GTRecipeCategories.get(chars.toString());
            return null;
        });

        typeWrappers.registerSimple(Element.class, o -> {
            if (o instanceof Element element) return element;
            if (o instanceof CharSequence chars) return GTElements.get(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(Material.class, o -> {
            if (o instanceof Material material) return material;
            if (o instanceof CharSequence chars) return GTMaterials.get(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(MachineDefinition.class, o -> {
            if (o instanceof MachineDefinition definition) return definition;
            if (o instanceof CharSequence chars) return GTMachines.get(chars.toString());
            return null;
        });

        typeWrappers.registerSimple(TagPrefix.class, o -> {
            if (o instanceof TagPrefix tagPrefix) return tagPrefix;
            if (o instanceof CharSequence chars) return TagPrefix.get(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(MaterialEntry.class, MaterialEntry::of);
        typeWrappers.registerSimple(RecipeCapability.class, o -> {
            if (o instanceof RecipeCapability<?> capability) return capability;
            if (o instanceof CharSequence chars) return GTRegistries.RECIPE_CAPABILITIES.get(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(ChanceLogic.class, o -> {
            if (o instanceof ChanceLogic capability) return capability;
            if (o instanceof CharSequence chars) return GTRegistries.CHANCE_LOGICS.get(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(ExtendedOutputItem.class, ExtendedOutputItem::of);

        typeWrappers.registerSimple(MaterialIconSet.class, o -> {
            if (o instanceof MaterialIconSet iconSet) return iconSet;
            if (o instanceof CharSequence chars) return MaterialIconSet.getByName(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(MaterialStack.class, o -> {
            if (o instanceof MaterialStack stack) return stack;
            if (o instanceof Material material) return new MaterialStack(material, 1);
            if (o instanceof CharSequence chars) return MaterialStack.fromString(chars);
            return null;
        });
        typeWrappers.registerSimple(MaterialStackWrapper.class, o -> {
            if (o instanceof MaterialStackWrapper wrapper) return wrapper;
            if (o instanceof MaterialStack stack) return new MaterialStackWrapper(stack::material, stack.amount());
            if (o instanceof Material material) return new MaterialStackWrapper(() -> material, 1);
            if (o instanceof CharSequence chars) return MaterialStackWrapper.fromString(chars);
            return null;
        });

        typeWrappers.registerSimple(IWorldGenLayer.class, o -> {
            if (o instanceof IWorldGenLayer layer) return layer;
            if (o instanceof CharSequence chars) return WorldGenLayers.getByName(chars.toString());
            return null;
        });
        typeWrappers.registerSimple(HeightRangePlacement.class, o -> {
            if (o instanceof HeightRangePlacement placement) return placement;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> HeightRangePlacement.CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.registerSimple(BiomeWeightModifier.class, o -> {
            if (o instanceof BiomeWeightModifier modifier) return modifier;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> BiomeWeightModifier.CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.registerSimple(VeinGenerator.class, o -> {
            if (o instanceof VeinGenerator generator) return generator;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> VeinGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.registerSimple(IndicatorGenerator.class, o -> {
            if (o instanceof IndicatorGenerator generator) return generator;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> IndicatorGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.registerSimple(IndicatorPlacement.class, o -> {
            if (o instanceof IndicatorPlacement placement) return placement;
            if (o instanceof CharSequence str) return IndicatorPlacement.getByName(str.toString());
            return null;
        });
        typeWrappers.registerSimple(MedicalCondition.class, o -> {
            if (o instanceof MedicalCondition condition) return condition;
            if (o instanceof CharSequence str) return MedicalCondition.CONDITIONS.get(str.toString());
            return null;
        });
        typeWrappers.registerSimple(IWorldGenLayer.RuleTestSupplier.class, o -> {
            if (o instanceof IWorldGenLayer.RuleTestSupplier supplier) return supplier;
            return () -> BlockStatePredicate.ruleTestOf(o);
        });
        typeWrappers.registerSimple(CraftingComponent.class, o -> {
            if (o instanceof CraftingComponent comp) return comp;
            if (o instanceof CharSequence str) return CraftingComponent.ALL_COMPONENTS.get(str.toString());
            return null;
        });
        typeWrappers.registerSimple(GTRecipeComponents.FluidIngredientJS.class,
                GTRecipeComponents.FluidIngredientJS::of);
        typeWrappers.registerSimple(EnergyStack.class, KJSHelpers::parseEnergyStack);
        typeWrappers.registerSimple(EnergyStack.WithIO.class, KJSHelpers::parseIOEnergyStack);
    }

    @Override
    public void injectRuntimeRecipes(RecipesEventJS event, RecipeManager manager,
                                     Map<ResourceLocation, Recipe<?>> recipesByName) {
        // (jankily) parse all GT recipes for extra ones to add, modify
        for (RecipeJS addedRecipe : event.addedRecipes) {
            if (addedRecipe instanceof GTRecipeSchema.GTRecipeJS gtRecipe) {
                handleGTRecipe(recipesByName, gtRecipe);
            } else if (addedRecipe instanceof GTShapedRecipeSchema.ShapedRecipeJS gtShaped) {
                handleGTShaped(gtShaped);
            }
        }

        PowerlessJetpack.FUELS.clear();
        // Must run recycling recipes very last
        RecyclingRecipes.init(builtRecipe -> recipesByName.put(builtRecipe.getId(),
                GTRecipeSerializer.SERIALIZER.fromJson(builtRecipe.getId(), builtRecipe.serializeRecipe())));
        ItemMaterialData.resolveItemMaterialInfos(builtRecipe -> recipesByName.put(builtRecipe.getId(),
                GTRecipeSerializer.SERIALIZER.fromJson(builtRecipe.getId(), builtRecipe.serializeRecipe())));

        // clone vanilla recipes for stuff like electric furnaces, etc
        for (RecipeType<?> recipeType : ForgeRegistries.RECIPE_TYPES) {
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                continue;
            }
            gtRecipeType.getLookup().removeAllRecipes();
            gtRecipeType.getProxyRecipes().forEach((type, list) -> {
                RecipeManagerHandler.addProxyRecipesToLookup(recipesByName, gtRecipeType, type, list);
            });
            RecipeManagerHandler.addRecipesToLookup(recipesByName, gtRecipeType);
        }
    }

    private static void handleGTRecipe(Map<ResourceLocation, Recipe<?>> recipesByName,
                                       GTRecipeSchema.GTRecipeJS gtRecipe) {
        GTRecipeType gtRecipeType = (GTRecipeType) ForgeRegistries.RECIPE_TYPES.getValue(gtRecipe.getType());
        if (gtRecipeType == null) {
            GTCEu.LOGGER.error("Failed to get GTRecipeType from GTRecipe: '{}' with type '{}'", gtRecipe.getId(),
                    gtRecipe.getType());
            return;
        }

        // get the recipe ID without the leading type path
        GTRecipeBuilder builder = gtRecipeType.recipeBuilder(gtRecipe.idWithoutType());
        if (gtRecipe.getValue(GTRecipeSchema.DURATION) != null) {
            builder.duration = gtRecipe.getValue(GTRecipeSchema.DURATION).intValue();
        }
        if (gtRecipe.getValue(GTRecipeSchema.DATA) != null) {
            builder.data = gtRecipe.getValue(GTRecipeSchema.DATA);
        }
        if (gtRecipe.getValue(GTRecipeSchema.CONDITIONS) != null) {
            builder.conditions.addAll(Arrays.stream(gtRecipe.getValue(GTRecipeSchema.CONDITIONS)).toList());
            builder.recipeType.setMinRecipeConditions(builder.conditions.size());
        }
        if (gtRecipe.getValue(GTRecipeSchema.CATEGORY) != null) {
            builder.recipeCategory = GTRegistries.RECIPE_CATEGORIES.get(gtRecipe.getValue(GTRecipeSchema.CATEGORY));
        }
        builder.researchRecipeEntries().addAll(gtRecipe.researchRecipeEntries());

        if (gtRecipe.getValue(GTRecipeSchema.ALL_INPUTS) != null) {
            builder.input.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_INPUTS).entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer
                                    .fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey())
                                            .getFirst().write(gtRecipe, content)))
                            .toList()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        if (gtRecipe.getValue(GTRecipeSchema.ALL_OUTPUTS) != null) {
            builder.output.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_OUTPUTS).entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer
                                    .fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey())
                                            .getSecond().write(gtRecipe, content)))
                            .toList()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        if (gtRecipe.getValue(GTRecipeSchema.ALL_TICK_INPUTS) != null) {
            builder.tickInput.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_TICK_INPUTS).entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer
                                    .fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey())
                                            .getFirst().write(gtRecipe, content)))
                            .toList()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        if (gtRecipe.getValue(GTRecipeSchema.ALL_TICK_OUTPUTS) != null) {
            builder.tickOutput.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_TICK_OUTPUTS).entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer
                                    .fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey())
                                            .getSecond().write(gtRecipe, content)))
                            .toList()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        if (gtRecipe.getValue(GTRecipeSchema.INPUT_CHANCE_LOGICS) != null) {
            builder.inputChanceLogic.putAll(gtRecipe.getValue(GTRecipeSchema.INPUT_CHANCE_LOGICS));
        }
        if (gtRecipe.getValue(GTRecipeSchema.OUTPUT_CHANCE_LOGICS) != null) {
            builder.outputChanceLogic.putAll(gtRecipe.getValue(GTRecipeSchema.OUTPUT_CHANCE_LOGICS));
        }
        if (gtRecipe.getValue(GTRecipeSchema.TICK_INPUT_CHANCE_LOGICS) != null) {
            builder.tickInputChanceLogic.putAll(gtRecipe.getValue(GTRecipeSchema.TICK_INPUT_CHANCE_LOGICS));
        }
        if (gtRecipe.getValue(GTRecipeSchema.TICK_OUTPUT_CHANCE_LOGICS) != null) {
            builder.tickOutputChanceLogic.putAll(gtRecipe.getValue(GTRecipeSchema.TICK_OUTPUT_CHANCE_LOGICS));
        }

        builder.setTempItemMaterialStacks(gtRecipe.itemMaterialStacks);
        builder.setTempFluidMaterialStacks(gtRecipe.fluidMaterialStacks);
        builder.setTempItemStacks(gtRecipe.tempItemStacks);
        gtRecipe.itemMaterialStacks = null;
        gtRecipe.fluidMaterialStacks = null;
        gtRecipe.tempItemStacks = null;

        builder.addMaterialInfo(gtRecipe.itemMaterialInfo, gtRecipe.fluidMaterialInfo);
        if (gtRecipe.removeMaterialInfo) {
            builder.removePreviousMaterialInfo();
        }

        builder.save(builtRecipe -> recipesByName.put(builtRecipe.getId(),
                GTRecipeSerializer.SERIALIZER.fromJson(builtRecipe.getId(), builtRecipe.serializeRecipe())));
    }

    private static void handleGTShaped(GTShapedRecipeSchema.ShapedRecipeJS shaped) {
        if (!shaped.isAddMaterialInfo()) return;

        var pattern = shaped.getValue(PATTERN);
        final var tools = ToolHelper.getToolSymbols();
        var key = shaped.getValue(KEY);
        var entries = key.entries();

        // Parse Material Info
        Char2IntOpenHashMap inputMap = new Char2IntOpenHashMap(entries.length);
        Char2IntMap slotMap = new Char2IntOpenHashMap(entries.length);
        int idx = -1;
        for (String s : pattern) {
            for (char c : s.toCharArray()) {
                ++idx;
                if (tools.contains(c)) continue; // Skip tools for decomp
                inputMap.addTo(c, 1);
                slotMap.put(c, idx);
            }
        }
        if (inputMap.isEmpty()) return;

        var result = shaped.getValue(RESULT);
        ItemStack outItem = result.item;
        int outCount = result.getCount();
        Reference2LongOpenHashMap<Material> materials = new Reference2LongOpenHashMap<>();
        CraftingContainer cc = new DummyCraftingContainer(new ItemStackHandler(idx + 1));

        for (var entry : entries) {
            char c = entry.key();
            int inCount = inputMap.get(c);
            if (inCount == 0) continue;

            var ingredient = entry.value().kjs$asIngredient();
            var values = ((IngredientAccessor) ingredient).getValues();
            if (values.length == 0 || values[0] instanceof Ingredient.TagValue) continue;

            ItemStack[] stacks = ingredient.getItems();
            ItemStack stack;
            if (stacks.length == 0 || (stack = stacks[0]).isEmpty()) continue;

            int slot = slotMap.get(c);
            cc.setItem(slot, stack);
            if (!IngredientAction.getRemaining(cc, slot, shaped.getIngredientActions()).isEmpty()) continue;

            var item = stack.getItem();

            var info = ItemMaterialData.getMaterialInfo(item);
            if (info != null) {
                for (var ms : info.getMaterials()) {
                    if (ms.material() instanceof MarkerMaterial) continue;
                    materials.addTo(ms.material(), (ms.amount() * inCount) / outCount);
                }
                continue;
            } else {
                ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.computeIfAbsent(outItem, i -> new ArrayList<>())
                        .add(stacks[0].copyWithCount(inCount));
            }

            var matStack = ChemicalHelper.getMaterialStack(item);
            if (!matStack.isEmpty() && !(matStack.material() instanceof MarkerMaterial)) {
                materials.addTo(matStack.material(), (matStack.amount() * inCount) / outCount);
            }

            var prefix = ChemicalHelper.getPrefix(item);
            if (!prefix.isEmpty()) {
                for (var ms : prefix.secondaryMaterials()) {
                    materials.addTo(ms.material(), (ms.amount() * inCount) / outCount);
                }
            }
        }

        ItemMaterialData.registerMaterialInfo(outItem.getItem(), new ItemMaterialInfo(materials));
    }
}
