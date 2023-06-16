package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.integration.kjs.builders.*;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.*;
import com.gregtechceu.gtceu.integration.kjs.builders.prefix.BasicTagPrefixBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.prefix.OreTagPrefixBuilder;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeBuilderJS;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeTypesEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import dev.latvian.mods.rhino.Wrapper;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.material.MaterialColor;

import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GregTechKubeJSPlugin
 */
public class GregTechKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        super.init();
        GTRegistryObjectBuilderTypes.ELEMENT.addType("basic", ElementBuilder.class, ElementBuilder::new, true);

        GTRegistryObjectBuilderTypes.MATERIAL_ICON_SET.addType("basic", MaterialIconSetBuilder.class, MaterialIconSetBuilder::new, true);
        GTRegistryObjectBuilderTypes.MATERIAL_ICON_TYPE.addType("basic", MaterialIconTypeBuilder.class, MaterialIconTypeBuilder::new, true);

        GTRegistryObjectBuilderTypes.MATERIAL.addType("basic", Material.Builder.class, Material.Builder::new, true);

        GTRegistryObjectBuilderTypes.RECIPE_TYPE.addType("basic", GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new, true);

        GTRegistryObjectBuilderTypes.MACHINE.addType("simple", SimpleMachineBuilder.class, SimpleMachineBuilder::new, true);
        GTRegistryObjectBuilderTypes.MACHINE.addType("steam", SteamMachineBuilder.class, SteamMachineBuilder::new, false);
        GTRegistryObjectBuilderTypes.MACHINE.addType("generator", GeneratorBuilder.class, GeneratorBuilder::new, false);
        GTRegistryObjectBuilderTypes.MACHINE.addType("multiblock", MultiblockBuilder.class, MultiblockBuilder::new, false);
        GTRegistryObjectBuilderTypes.MACHINE.addType("kinetic", KineticMachineBuilder.class, KineticMachineBuilder::new, false);

        GTRegistryObjectBuilderTypes.WORLD_GEN_LAYER.addType("basic", WorldGenLayerBuilder.class, WorldGenLayerBuilder::new, true);

        GTRegistryObjectBuilderTypes.TAG_PREFIX.addType("basic", BasicTagPrefixBuilder.class, BasicTagPrefixBuilder::new, true);
        GTRegistryObjectBuilderTypes.TAG_PREFIX.addType("ore", OreTagPrefixBuilder.class, OreTagPrefixBuilder::new, false);

        RegistryObjectBuilderTypes.BLOCK.addType("gtceu:coil", CoilBlockBuilder.class, CoilBlockBuilder::new);
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
    public void registerRecipeTypes(RegisterRecipeTypesEvent event) {
        super.registerRecipeTypes(event);

        event.register(GTCEu.id("gt_recipe_serializer"), GTRecipeBuilderJS::new);
        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), GTRecipeBuilderJS::new);
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
        event.add("GTMachines", GTMachines.class);
        event.add("GTItems", GTItems.class);
        event.add("GTMaterialBuilder", Material.Builder.class);
        event.add("GTRecipeTypes", GTRecipeTypes.class);
        event.add("TagPrefix", TagPrefix.class);
        event.add("ItemGenerationCondition", TagPrefix.Conditions.class);
        event.add("UnificationEntry", UnificationEntry.class);
        event.add("RecipeCapability", RecipeCapability.class);
        event.add("GTFluidTypes", FluidTypes.class);

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
        event.add("Material", net.minecraft.world.level.material.Material.class);
        event.add("MaterialColor", MaterialColor.class);
        event.add("SoundType", SoundType.class);

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
            if (o instanceof CharSequence chars) return TagPrefix.getPrefix(chars.toString());
            return null;
        });
        typeWrappers.register(UnificationEntry.class, (ctx, o) -> {
            if (o instanceof UnificationEntry entry) return entry;
            if (o instanceof CharSequence chars) {
                var values = chars.toString().split(":");
                if (values.length == 1) {
                    return new UnificationEntry(TagPrefix.getPrefix(values[0]));
                }
                if (values.length >= 2) {
                    return new UnificationEntry(TagPrefix.getPrefix(values[0]), GTMaterials.get(values[1]));
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

        typeWrappers.register(IWorldGenLayer.class, (ctx, o) -> {
            if (o instanceof IWorldGenLayer layer) return layer;
            if (o instanceof CharSequence chars) return WorldGenLayers.getByName(chars.toString());
            return null;
        });
        typeWrappers.register(HeightRangePlacement.class, (ctx, o) -> {
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> HeightRangePlacement.CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.register(BiomeWeightModifier.class, (ctx, o) -> {
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> BiomeWeightModifier.CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
        typeWrappers.register(GTOreFeatureEntry.VeinGenerator.class, (ctx, o) -> {
            return Optional.ofNullable(NBTUtils.toTagCompound(o))
                    .map(tag -> GTOreFeatureEntry.VeinGenerator.DIRECT_CODEC.parse(NbtOps.INSTANCE, tag))
                    .flatMap(DataResult::result)
                    .orElse(null);
        });
    }

}
