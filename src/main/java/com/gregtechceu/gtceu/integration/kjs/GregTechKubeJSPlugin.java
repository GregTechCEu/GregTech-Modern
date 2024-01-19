package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
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
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.common.data.*;
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
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GregTechKubeJSPlugin
 */
public class GregTechKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void initStartup() {
        super.initStartup();
    }

    @Override
    public void init() {
        super.init();
        GTRegistryInfo.ELEMENT.addType("basic", ElementBuilder.class, ElementBuilder::new, true);

        GTRegistryInfo.MATERIAL_ICON_SET.addType("basic", MaterialIconSetBuilder.class, MaterialIconSetBuilder::new, true);
        GTRegistryInfo.MATERIAL_ICON_TYPE.addType("basic", MaterialIconTypeBuilder.class, MaterialIconTypeBuilder::new, true);

        GTRegistryInfo.MATERIAL.addType("basic", Material.Builder.class, Material.Builder::new, true);

        GTRegistryInfo.RECIPE_TYPE.addType("basic", GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new, true);

        GTRegistryInfo.MACHINE.addType("simple", SimpleMachineBuilder.class, (id, args) -> SimpleMachineBuilder.createAll(id.getPath(), args), true);
        GTRegistryInfo.MACHINE.addType("custom", CustomTieredMachineBuilder.class, (id, args) -> CustomTieredMachineBuilder.createAll(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("steam", SteamMachineBuilder.class, (id, args) -> SteamMachineBuilder.createBoth(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("generator", GeneratorBuilder.class, (id, args) -> GeneratorBuilder.createAll(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("multiblock", CustomMultiblockBuilder.class, (id, args) -> CustomMultiblockBuilder.createMultiblock(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("primitive", CustomMultiblockBuilder.class, (id, args) -> CustomMultiblockBuilder.createPrimitiveMultiblock(id.getPath(), args), false);
        GTRegistryInfo.MACHINE.addType("kinetic", KineticMachineBuilder.class, (id, args) -> KineticMachineBuilder.createAll(id.getPath(), args), false);

        GTRegistryInfo.WORLD_GEN_LAYER.addType("basic", WorldGenLayerBuilder.class, WorldGenLayerBuilder::new, true);

        GTRegistryInfo.TAG_PREFIX.addType("basic", BasicTagPrefixBuilder.class, BasicTagPrefixBuilder::new, true);
        GTRegistryInfo.TAG_PREFIX.addType("ore", OreTagPrefixBuilder.class, OreTagPrefixBuilder::new, false);

        RegistryInfo.BLOCK.addType("gtceu:coil", CoilBlockBuilder.class, CoilBlockBuilder::new);
        RegistryInfo.BLOCK.addType("gtceu:renderer", RendererBlockBuilder.class, RendererBlockBuilder::new);
        RegistryInfo.BLOCK.addType("gtceu:renderer_glass", RendererGlassBlockBuilder.class, RendererGlassBlockBuilder::new);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        GTCEuStartupEvents.GROUP.register();
        GTCEuServerEvents.GROUP.register();
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
        // allow user to access all gtceu classes by importing them.
        filter.allow("com.gregtechceu.gtceu");
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        super.registerRecipeSchemas(event);

        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), GTRecipeSchema.SCHEMA);
        }
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
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
        event.add("TagPrefix", TagPrefix.class);
        event.add("ItemGenerationCondition", TagPrefix.Conditions.class);
        event.add("UnificationEntry", UnificationEntry.class);
        event.add("RecipeCapability", RecipeCapability.class);
        event.add("GTFluidAttributes", FluidAttributes.class);
        event.add("GTFluidBuilder", FluidBuilder.class);
        event.add("GTFluidStorageKeys", FluidStorageKeys.class);
        event.add("GTFluidState", FluidState.class);
        event.add("PropertyKey", PropertyKey.class);
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

        // MaterialColor stuff, for TagPrefix
        event.add("SoundType", SoundType.class);

        event.add("GTOreVein", GTOreDefinition.class);
        event.add("GTLayerPattern", GTLayerPattern.class);
        event.add("GTDikeBlockDefinition", DikeVeinGenerator.DikeBlockDefinition.class);
        event.add("GTOres", GTOres.class);
        event.add("GTRecipeModifiers", GTRecipeModifiers.class);
        event.add("OverclockingLogic", OverclockingLogic.class);
        event.add("GTWorldGenLayers", WorldGenLayers.class);
        // ....TODO add global refs. for convenience, ppl do not need to import the java package themselves.
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        super.registerTypeWrappers(type, typeWrappers);
        typeWrappers.register(GTRecipeType.class, (ctx, o) -> {
            if (o instanceof Wrapper w) {
                o = w.unwrap();
            }
            if (o instanceof GTRecipeType recipeType) return recipeType;
            if (o instanceof CharSequence chars) return GTRecipeTypes.get(chars.toString());
            return null;
        });

        typeWrappers.register(Element.class, (ctx, o) -> {
            if (o instanceof Element element) return element;
            if (o instanceof CharSequence chars) return GTElements.get(chars.toString());
            return null;
        });
        typeWrappers.register(Material.class, (ctx, o) -> {
            if (o instanceof Material material) return material;
            if (o instanceof CharSequence chars) return GTMaterials.get(chars.toString());
            return null;
        });
        typeWrappers.register(MachineDefinition.class, (ctx, o) -> {
            if (o instanceof MachineDefinition definition) return definition;
            if (o instanceof CharSequence chars) return GTMachines.get(chars.toString());
            return null;
        });

        typeWrappers.register(TagPrefix.class, (ctx, o) -> {
            if (o instanceof TagPrefix tagPrefix) return tagPrefix;
            if (o instanceof CharSequence chars) return TagPrefix.get(chars.toString());
            return null;
        });
        typeWrappers.register(UnificationEntry.class, (ctx, o) -> {
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
        typeWrappers.register(RecipeCapability.class, (ctx, o) -> {
            if (o instanceof RecipeCapability<?> capability) return capability;
            if (o instanceof CharSequence chars) return GTRegistries.RECIPE_CAPABILITIES.get(chars.toString());
            return null;
        });

        typeWrappers.register(MaterialIconSet.class, (ctx, o) -> {
            if (o instanceof MaterialIconSet iconSet) return iconSet;
            if (o instanceof CharSequence chars) return MaterialIconSet.getByName(chars.toString());
            return null;
        });
        typeWrappers.register(MaterialStack.class, (ctx, o) -> {
            if (o instanceof MaterialStack stack) return stack;
            if (o instanceof Material material) return new MaterialStack(material, 1);
            if (o instanceof CharSequence chars) return MaterialStack.fromString(chars);
            return null;
        });
        typeWrappers.register(MaterialStackWrapper.class, (ctx, o) -> {
            if (o instanceof MaterialStackWrapper wrapper) return wrapper;
            if (o instanceof MaterialStack stack) return new MaterialStackWrapper(stack::material, stack.amount());
            if (o instanceof Material material) return new MaterialStackWrapper(() -> material, 1);
            if (o instanceof CharSequence chars) return MaterialStackWrapper.fromString(chars);
            return null;
        });

        typeWrappers.register(IWorldGenLayer.class, (ctx, o) -> {
            if (o instanceof IWorldGenLayer layer) return layer;
            if (o instanceof CharSequence chars) return WorldGenLayers.getByName(chars.toString());
            return null;
        });
        typeWrappers.register(HeightRangePlacement.class, (ctx, o) -> {
            if (o instanceof HeightRangePlacement placement) return placement;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> HeightRangePlacement.CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.register(BiomeWeightModifier.class, (ctx, o) -> {
            if (o instanceof BiomeWeightModifier modifier) return modifier;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> BiomeWeightModifier.CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.register(VeinGenerator.class, (ctx, o) -> {
            if (o instanceof VeinGenerator generator) return generator;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> VeinGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.register(IndicatorGenerator.class, (ctx, o) -> {
            if (o instanceof IndicatorGenerator generator) return generator;
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> IndicatorGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.register(IndicatorPlacement.class, (ctx, o) -> {
            if (o instanceof IndicatorPlacement placement) return placement;
            if (o instanceof CharSequence str) return IndicatorPlacement.getByName(str.toString());
            return null;
        });
        // jank because Rhino doesn't agree that it's an interface
        typeWrappers.register(IWorldGenLayer.RuleTestSupplier.class, (ctx, o) -> {
            if (o instanceof IWorldGenLayer.RuleTestSupplier supplier) return supplier;
            return () -> BlockStatePredicate.ruleTestOf(o);
        });
        typeWrappers.registerSimple(GTRecipeComponents.FluidIngredientJS.class, GTRecipeComponents.FluidIngredientJS::of);
    }

}
