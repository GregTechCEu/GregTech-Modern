package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import net.minecraftforge.fml.ModLoader;

public class GTRecipeCategories {

    // Used for recipes you don't want in a category
    public static final GTRecipeCategory DUMMY = new GTRecipeCategory(GTRecipeTypes.DUMMY_RECIPES);

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = GTRecipeCategory.register("arc_furnace_recycling", GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory.register("macerator_recycling", GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(GuiTextures.MACERATOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = GTRecipeCategory.register("extractor_recycling", GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(GuiTextures.EXTRACTOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory ORE_CRUSHING = GTRecipeCategory.register("ore_crushing", GTRecipeTypes.MACERATOR_RECIPES);

    public static final GTRecipeCategory ORE_FORGING = GTRecipeCategory.register("ore_forging", GTRecipeTypes.FORGE_HAMMER_RECIPES);

    public static final GTRecipeCategory ORE_BATHING = GTRecipeCategory.register("ore_bathing", GTRecipeTypes.CHEMICAL_BATH_RECIPES);

    public static final GTRecipeCategory CHEM_DYES = GTRecipeCategory.register("chem_dyes", GTRecipeTypes.CHEMICAL_BATH_RECIPES);

    public static final GTRecipeCategory INGOT_MOLDING = GTRecipeCategory.register("ingot_molding", GTRecipeTypes.ALLOY_SMELTER_RECIPES);

    public static void init() {
        GTRegistries.RECIPE_CATEGORIES.remove(GTRecipeTypes.FURNACE_RECIPES.getCategory().registryKey);
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.RECIPE_CATEGORIES.getRegistryName());
        }
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_CATEGORIES, GTRecipeCategory.class));
        GTRegistries.RECIPE_CATEGORIES.freeze();
    }

    public static GTRecipeCategory get(String name) {
        return GTRegistries.RECIPE_CATEGORIES.get(GTCEu.appendId(name));
    }
}
