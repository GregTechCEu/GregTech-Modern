package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.RotationState;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.fluid.FluidBuilder;
import com.gregtechceu.gtceu.api.fluid.FluidState;
import com.gregtechceu.gtceu.api.fluid.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.Element;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.multiblock.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.worldgen.*;
import com.gregtechceu.gtceu.api.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.indicators.SurfaceIndicatorGenerator.IndicatorPlacement;
import com.gregtechceu.gtceu.api.worldgen.generator.veins.DikeVeinGenerator;
import com.gregtechceu.gtceu.common.material.MaterialRegistryManager;
import com.gregtechceu.gtceu.data.block.GCyMBlocks;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.block.GTOres;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.machine.GCyMMachines;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.data.material.GTElements;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.medicalcondition.GTMedicalConditions;
import com.gregtechceu.gtceu.data.recipe.GTRecipeModifiers;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.sound.GTSoundEntries;
import com.gregtechceu.gtceu.integration.kjs.builders.*;
import com.gregtechceu.gtceu.integration.kjs.builders.block.CoilBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.block.RendererBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.block.RendererGlassBlockBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.*;
import com.gregtechceu.gtceu.integration.kjs.builders.prefix.BasicTagPrefixBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.prefix.OreTagPrefixBuilder;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.neoforged.neoforge.common.conditions.ICondition;

import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.latvian.mods.kubejs.util.NBTUtils;
import dev.latvian.mods.rhino.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GregTechKubeJSPlugin
 */
public class GregTechKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        GTRegistryInfo.ELEMENT.addType("basic", ElementBuilder.class, ElementBuilder::new, true);

        GTRegistryInfo.MATERIAL_ICON_SET.addType("basic", MaterialIconSetBuilder.class, MaterialIconSetBuilder::new,
                true);
        GTRegistryInfo.MATERIAL_ICON_TYPE.addType("basic", MaterialIconTypeBuilder.class, MaterialIconTypeBuilder::new,
                true);

        GTRegistryInfo.MATERIAL.addType("basic", Material.Builder.class, Material.Builder::new, true);

        GTRegistryInfo.RECIPE_TYPE.addType("basic", GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new, true);

        GTRegistryInfo.MACHINE.addType("simple", SimpleMachineBuilder.class,
                (id, args) -> SimpleMachineBuilder.create(id.getPath(), args), true);
        GTRegistryInfo.MACHINE.addType("custom", CustomTieredMachineBuilder.class,
                (id, args) -> CustomTieredMachineBuilder.createAll(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("steam", SteamMachineBuilder.class,
                (id, args) -> SteamMachineBuilder.createBoth(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("generator", GeneratorBuilder.class,
                (id, args) -> GeneratorBuilder.createAll(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("multiblock", CustomMultiblockBuilder.class,
                (id, args) -> CustomMultiblockBuilder.createMultiblock(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("primitive", CustomMultiblockBuilder.class,
                (id, args) -> CustomMultiblockBuilder.createPrimitiveMultiblock(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("kinetic", KineticMachineBuilder.class,
                (id, args) -> KineticMachineBuilder.createAll(id.getPath(), args), false);

        GTRegistryInfo.WORLD_GEN_LAYER.addType("basic", WorldGenLayerBuilder.class, WorldGenLayerBuilder::new, true);

        GTRegistryInfo.TAG_PREFIX.addType("basic", BasicTagPrefixBuilder.class, BasicTagPrefixBuilder::new, true);
        GTRegistryInfo.TAG_PREFIX.addType("ore", OreTagPrefixBuilder.class, OreTagPrefixBuilder::new, false);

        registry.of(Registries.BLOCK, reg -> {
            reg.add("gtceu:coil", CoilBlockBuilder.class, CoilBlockBuilder::new);
            reg.add("gtceu:renderer", RendererBlockBuilder.class, RendererBlockBuilder::new);
            reg.add("gtceu:renderer_glass", RendererGlassBlockBuilder.class, RendererGlassBlockBuilder::new);
        });
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(GTCEuStartupEvents.GROUP);
        registry.register(GTCEuServerEvents.GROUP);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        // allow user to access all gtceu classes by importing them.
        filter.allow("com.gregtechceu.gtceu");
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            registry.register(entry.getKey(), GTRecipeSchema.SCHEMA);
        }
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistry event) {
        event.register(GTRecipeComponents.TAG);
        event.register(GTRecipeComponents.RECIPE_CONDITION);
        event.register(GTRecipeComponents.RESOURCE_LOCATION);
        event.register(GTRecipeComponents.IN);
        event.register(GTRecipeComponents.TICK_IN);
        event.register(GTRecipeComponents.OUT);
        event.register(GTRecipeComponents.TICK_OUT);

        event.register(GTRecipeComponents.ITEM);
        event.register(GTRecipeComponents.FLUID);
        event.register(GTRecipeComponents.EU);
        event.register(GTRecipeComponents.SU);
    }

    @Override
    public void registerBindings(BindingRegistry event) {
        event.add("GTRegistries", GTRegistries.class);
        event.add("GTMaterials", GTMaterials.class);
        event.add("GTElements", GTElements.class);
        event.add("GTSoundEntries", GTSoundEntries.class);
        event.add("GTBlocks", GTBlocks.class);
        event.add("GCyMBlocks", GCyMBlocks.class);
        event.add("GTMachines", GTMachines.class);
        event.add("GCyMMachines", GCyMMachines.class);
        event.add("GTItems", GTItems.class);
        event.add("GTRecipeTypes", GTRecipeTypes.class);
        event.add("GTMedicalConditions", GTMedicalConditions.class);
        event.add("TagPrefix", TagPrefix.class);
        event.add("ItemGenerationCondition", TagPrefix.Conditions.class);
        event.add("UnificationEntry", UnificationEntry.class);
        event.add("RecipeCapability", RecipeCapability.class);
        event.add("GTFluidAttributes", FluidAttributes.class);
        event.add("GTFluidBuilder", FluidBuilder.class);
        event.add("GTFluidStorageKeys", FluidStorageKeys.class);
        event.add("GTFluidState", FluidState.class);
        event.add("PropertyKey", PropertyKey.class);
        event.add("ToolProperty", ToolProperty.class);
        event.add("HazardProperty", HazardProperty.class);
        event.add("MedicalCondition", MedicalCondition.class);
        event.add("Symptom", Symptom.class);
        event.add("CleanroomType", CleanroomType.class);
        event.add("ChemicalHelper", ChemicalHelper.class);

        event.add("GTValues", GTValues.class);
        event.add("GTMaterialIconSet", MaterialIconSet.class);
        event.add("GTMaterialIconType", MaterialIconType.class);
        event.add("GTMaterialFlags", MaterialFlags.class);
        event.add("GTToolType", GTToolType.class);
        event.add("RotationState", RotationState.class);
        event.add("FactoryBlockPattern", FactoryBlockPattern.class);
        event.add("MultiblockShapeInfo", MultiblockShapeInfo.class);
        event.add("Predicates", Predicates.class);
        event.add("PartAbility", PartAbility.class);
        event.add("GuiTextures", GuiTextures.class);
        event.add("GTCEu", GTCEu.class);
        event.add("GTCEuAPI", GTCEuAPI.class);
        event.add("GTMaterialRegistry", MaterialRegistryManager.getInstance());

        // MaterialColor stuff, for TagPrefix
        event.add("SoundType", SoundType.class);

        event.add("GTOreVein", GTOreDefinition.class);
        event.add("GTLayerPattern", GTLayerPattern.class);
        event.add("GTDikeBlockDefinition", DikeVeinGenerator.DikeBlockDefinition.class);
        event.add("GTOres", GTOres.class);
        event.add("GTRecipeModifiers", GTRecipeModifiers.class);
        event.add("OverclockingLogic", OverclockingLogic.class);
        event.add("GTWorldGenLayers", WorldGenLayers.class);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        KubeJSPlugin.super.registerTypeWrappers(registry);
        registry.register(GTRecipeType.class, (TypeWrapperRegistry.ContextFromFunction<GTRecipeType>) (ctx, o) -> {
            if (o instanceof Wrapper w) {
                o = w.unwrap();
            }
            if (o instanceof GTRecipeType recipeType) return recipeType;
            if (o instanceof CharSequence chars) return GTRecipeTypes.get(chars.toString());
            return null;
        });

        registry.register(Element.class, (TypeWrapperRegistry.ContextFromFunction<Element>) (ctx, o) -> {
            if (o instanceof Element element) return element;
            if (o instanceof CharSequence chars) return GTElements.get(chars.toString());
            return null;
        });
        registry.register(Material.class, (TypeWrapperRegistry.ContextFromFunction<Material>) (ctx, o) -> {
            if (o instanceof Material material) return material;
            if (o instanceof CharSequence chars) return GTMaterials.get(chars.toString());
            return null;
        });
        registry.register(MachineDefinition.class,
                (TypeWrapperRegistry.ContextFromFunction<MachineDefinition>) (ctx, o) -> {
                    if (o instanceof MachineDefinition definition) return definition;
                    if (o instanceof CharSequence chars) return GTMachines.get(chars.toString());
                    return null;
                });

        registry.register(TagPrefix.class, (TypeWrapperRegistry.ContextFromFunction<TagPrefix>) (ctx, o) -> {
            if (o instanceof TagPrefix tagPrefix) return tagPrefix;
            if (o instanceof CharSequence chars) return TagPrefix.get(chars.toString());
            return null;
        });
        registry.register(UnificationEntry.class,
                (TypeWrapperRegistry.ContextFromFunction<UnificationEntry>) (ctx, o) -> {
                    if (o instanceof UnificationEntry entry) return entry;
                    if (o instanceof CharSequence chars) {
                        var values = chars.toString().split(":");
                        if (values.length == 1) {
                            return new UnificationEntry(TagPrefix.get(values[0]));
                        }
                        if (values.length >= 2) {
                            return new UnificationEntry(TagPrefix.get(values[0]), GTMaterials.get(values[1]));
                        }
                    }
                    return null;
                });
        // noinspection rawtypes
        registry.register(RecipeCapability.class,
                (TypeWrapperRegistry.ContextFromFunction<RecipeCapability>) (ctx, o) -> {
                    if (o instanceof RecipeCapability<?> capability) return capability;
                    if (o instanceof CharSequence chars) return GTRegistries.RECIPE_CAPABILITIES.get(chars.toString());
                    return null;
                });

        registry.register(MaterialIconSet.class,
                (TypeWrapperRegistry.ContextFromFunction<MaterialIconSet>) (ctx, o) -> {
                    if (o instanceof MaterialIconSet iconSet) return iconSet;
                    if (o instanceof CharSequence chars) return MaterialIconSet.getByName(chars.toString());
                    return null;
                });
        registry.register(MaterialStack.class, (TypeWrapperRegistry.ContextFromFunction<MaterialStack>) (ctx, o) -> {
            if (o instanceof MaterialStack stack) return stack;
            if (o instanceof Material material) return new MaterialStack(material, 1);
            if (o instanceof CharSequence chars) return MaterialStack.fromString(chars);
            return null;
        });
        registry.register(MaterialStackWrapper.class,
                (TypeWrapperRegistry.ContextFromFunction<MaterialStackWrapper>) (ctx, o) -> {
                    if (o instanceof MaterialStackWrapper wrapper) return wrapper;
                    if (o instanceof MaterialStack stack)
                        return new MaterialStackWrapper(stack::material, stack.amount());
                    if (o instanceof Material material) return new MaterialStackWrapper(() -> material, 1);
                    if (o instanceof CharSequence chars) return MaterialStackWrapper.fromString(chars);
                    return null;
                });

        registry.register(IWorldGenLayer.class, (TypeWrapperRegistry.ContextFromFunction<IWorldGenLayer>) (ctx, o) -> {
            if (o instanceof IWorldGenLayer layer) return layer;
            if (o instanceof CharSequence chars) return WorldGenLayers.getByName(chars.toString());
            return null;
        });
        registry.register(HeightRangePlacement.class,
                (TypeWrapperRegistry.ContextFromFunction<HeightRangePlacement>) (ctx, o) -> {
                    if (o instanceof HeightRangePlacement placement) return placement;
                    return Optional.ofNullable(NBTUtils.toTagCompound(ctx, o))
                            .map(tag -> HeightRangePlacement.CODEC.codec().parse(NbtOps.INSTANCE, tag))
                            .flatMap(DataResult::result)
                            .orElse(null);
                });
        registry.register(BiomeWeightModifier.class,
                (TypeWrapperRegistry.ContextFromFunction<BiomeWeightModifier>) (ctx, o) -> {
                    if (o instanceof BiomeWeightModifier modifier) return modifier;
                    return Optional.ofNullable(NBTUtils.toTagCompound(ctx, o))
                            .map(tag -> BiomeWeightModifier.CODEC.parse(NbtOps.INSTANCE, tag))
                            .flatMap(DataResult::result)
                            .orElse(null);
                });
        registry.register(VeinGenerator.class, (TypeWrapperRegistry.ContextFromFunction<VeinGenerator>) (ctx, o) -> {
            if (o instanceof VeinGenerator generator) return generator;
            return Optional.ofNullable(NBTUtils.toTagCompound(ctx, o))
                    .map(tag -> VeinGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        registry.register(IndicatorGenerator.class,
                (TypeWrapperRegistry.ContextFromFunction<IndicatorGenerator>) (ctx, o) -> {
                    if (o instanceof IndicatorGenerator generator) return generator;
                    return Optional.ofNullable(NBTUtils.toTagCompound(ctx, o))
                            .map(tag -> IndicatorGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                            .flatMap(DataResult::result)
                            .orElse(null);
                });
        registry.register(IndicatorPlacement.class,
                (TypeWrapperRegistry.ContextFromFunction<IndicatorPlacement>) (ctx, o) -> {
                    if (o instanceof IndicatorPlacement placement) return placement;
                    if (o instanceof CharSequence str) return IndicatorPlacement.getByName(str.toString());
                    return null;
                });
        registry.register(MedicalCondition.class,
                (TypeWrapperRegistry.ContextFromFunction<MedicalCondition>) (ctx, o) -> {
                    if (o instanceof MedicalCondition condition) return condition;
                    if (o instanceof CharSequence str) return MedicalCondition.CONDITIONS.get(str.toString());
                    return null;
                });
        // jank because Rhino doesn't agree that it's an interface
        registry.register(IWorldGenLayer.RuleTestSupplier.class,
                (TypeWrapperRegistry.ContextFromFunction<IWorldGenLayer.RuleTestSupplier>) (ctx, o) -> {
                    if (o instanceof IWorldGenLayer.RuleTestSupplier supplier) return supplier;
                    return () -> BlockStatePredicate.ruleTestOf(ctx, o);
                });
    }

    @Override
    public void injectRuntimeRecipes(RecipesKubeEvent event, RecipeManagerKJS manager,
                                     Map<ResourceLocation, RecipeHolder<?>> recipesByName) {
        // (jankily) parse all GT recipes for extra ones to add, modify
        RecipesKubeEvent.runInParallel((() -> event.addedRecipes.forEach(recipe -> {
            if (recipe instanceof GTRecipeSchema.GTKubeRecipe gtRecipe) {
                // get the recipe ID without the leading type path
                GTRecipeBuilder builder = ((GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(gtRecipe.type.id))
                        .recipeBuilder(gtRecipe.idWithoutType());

                if (gtRecipe.getValue(GTRecipeSchema.DURATION) != null) {
                    builder.duration = (int) gtRecipe.getValue(GTRecipeSchema.DURATION).ticks();
                }
                if (gtRecipe.getValue(GTRecipeSchema.DATA) != null) {
                    builder.data = gtRecipe.getValue(GTRecipeSchema.DATA);
                }
                if (gtRecipe.getValue(GTRecipeSchema.CONDITIONS) != null) {
                    builder.conditions.addAll(gtRecipe.getValue(GTRecipeSchema.CONDITIONS));
                }
                if (gtRecipe.getValue(GTRecipeSchema.IS_FUEL) != null) {
                    builder.isFuel = gtRecipe.getValue(GTRecipeSchema.IS_FUEL);
                }
                builder.researchRecipeEntries().addAll(gtRecipe.researchRecipeEntries());

                if (gtRecipe.getValue(GTRecipeSchema.ALL_INPUTS) != null) {
                    builder.input.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_INPUTS));
                }
                if (gtRecipe.getValue(GTRecipeSchema.ALL_OUTPUTS) != null) {
                    builder.output.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_OUTPUTS));
                }
                if (gtRecipe.getValue(GTRecipeSchema.ALL_TICK_INPUTS) != null) {
                    builder.tickInput.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_TICK_INPUTS));
                }
                if (gtRecipe.getValue(GTRecipeSchema.ALL_TICK_OUTPUTS) != null) {
                    builder.tickOutput.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_TICK_OUTPUTS));
                }

                builder.save(new RecipeOutput() {

                    @Override
                    public Advancement.Builder advancement() {
                        // noinspection removal
                        return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                    }

                    @Override
                    public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                                       @Nullable AdvancementHolder advancement,
                                       ICondition @NotNull... conditions) {
                        recipesByName.put(id, new RecipeHolder<>(id, recipe));
                    }
                });
            }
        })));

        // clone vanilla recipes for stuff like electric furnaces, etc
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.getLookup().removeAllRecipes();

                var proxyRecipes = gtRecipeType.getProxyRecipes();
                for (Map.Entry<RecipeType<?>, List<RecipeHolder<GTRecipe>>> entry : proxyRecipes.entrySet()) {
                    var type = entry.getKey();
                    var recipes = entry.getValue();
                    recipes.clear();
                    for (var recipe : recipesByName.entrySet().stream()
                            .filter(recipe -> recipe.getValue().value().getType() == type)
                            .collect(Collectors.toSet())) {
                        recipes.add(gtRecipeType.toGTRecipe(recipe.getValue()));
                    }
                }

                // noinspection unchecked
                Stream.concat(
                        recipesByName.values().stream()
                                .filter(recipeHolder -> recipeHolder.value().getType() == gtRecipeType),
                        proxyRecipes.entrySet().stream()
                                .flatMap(entry -> entry.getValue().stream()))
                        .filter(holder -> holder != null && holder.value() instanceof GTRecipe)
                        .forEach(gtRecipe -> gtRecipeType.getLookup().addRecipe((RecipeHolder<GTRecipe>) gtRecipe));
            }
        }
    }
}
