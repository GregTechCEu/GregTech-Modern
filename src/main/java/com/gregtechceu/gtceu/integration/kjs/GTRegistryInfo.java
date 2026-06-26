package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.builders.material.ElementBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.material.MaterialBuilderWrapper;
import com.gregtechceu.gtceu.integration.kjs.builders.material.MaterialIconSetBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.recipe.GTRecipeCategoryBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.recipe.GTRecipeTypeBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class GTRegistryInfo {

    public static final RegistryInfo<Element> ELEMENT = RegistryInfo.of(GTRegistries.Keys.ELEMENT, Element.class);
    public static final RegistryInfo<MaterialIconSet> MATERIAL_ICON_SET = RegistryInfo.of(GTRegistries.Keys.MATERIAL_ICON_SET, MaterialIconSet.class);
    public static final RegistryInfo<Material> MATERIAL = RegistryInfo.of(GTRegistries.Keys.MATERIAL, Material.class);
    public static final RegistryInfo<GTRecipeType> RECIPE_TYPE = RegistryInfo.of(GTRegistries.Keys.RECIPE_TYPE, GTRecipeType.class);
    public static final RegistryInfo<GTRecipeCategory> RECIPE_CATEGORY = RegistryInfo.of(GTRegistries.Keys.RECIPE_CATEGORY, GTRecipeCategory.class);

    public static void init() {
        ELEMENT.addType("basic", ElementBuilder.class, ElementBuilder::new, true);
        MATERIAL_ICON_SET.addType("basic", MaterialIconSetBuilder.class, MaterialIconSetBuilder::new,
                true);
        MATERIAL.addType("basic", MaterialBuilderWrapper.class, MaterialBuilderWrapper::new);
        RECIPE_TYPE.addType("basic", GTRecipeTypeBuilder.class, GTRecipeTypeBuilder::new);
        RECIPE_CATEGORY.addType("basic", GTRecipeCategoryBuilder.class, GTRecipeCategoryBuilder::new);
    }

}
