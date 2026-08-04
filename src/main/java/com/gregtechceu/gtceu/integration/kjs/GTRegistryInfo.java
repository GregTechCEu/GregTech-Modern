package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class GTRegistryInfo {

    // spotless:off
    public static final RegistryInfo<Element> ELEMENT = RegistryInfo.of(GTRegistries.Keys.ELEMENT, Element.class);
    public static final RegistryInfo<Material> MATERIAL = RegistryInfo.of(GTRegistries.Keys.MATERIAL, Material.class);
    public static final RegistryInfo<GTRecipeType> RECIPE_TYPE = RegistryInfo.of(GTRegistries.Keys.RECIPE_TYPE,
            GTRecipeType.class);
    public static final RegistryInfo<GTRecipeCategory> RECIPE_CATEGORY = RegistryInfo
            .of(GTRegistries.Keys.RECIPE_CATEGORY, GTRecipeCategory.class);
    public static final RegistryInfo<MachineDefinition> MACHINE = RegistryInfo.of(GTRegistries.Keys.MACHINE,
            MachineDefinition.class);
    public static final RegistryInfo<MaterialIconSet> MATERIAL_ICON_SET = RegistryInfo
            .of(GTRegistries.Keys.MATERIAL_ICON_SET, MaterialIconSet.class);
    public static final RegistryInfo<IWorldGenLayer> WORLD_GEN_LAYER = RegistryInfo
            .of(GTRegistries.Keys.WORLD_GEN_LAYER, IWorldGenLayer.class);
    public static final RegistryInfo<TagPrefix> TAG_PREFIX = RegistryInfo.of(GTRegistries.Keys.TAG_PREFIX,
            TagPrefix.class);
    public static final RegistryInfo<DimensionMarker> DIMENSION_MARKER = RegistryInfo
            .of(GTRegistries.Keys.DIMENSION_MARKER, DimensionMarker.class);
}
