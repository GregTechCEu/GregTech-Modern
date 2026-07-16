package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTRecipeCategories {

    public static final GTRecipeCategory ORE_CRUSHING = REGISTRATE.recipeCategory("ore_crushing",
            GTRecipeTypes.MACERATOR_RECIPES);
    public static final GTRecipeCategory ORE_FORGING = REGISTRATE.recipeCategory("ore_forging",
            GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public static final GTRecipeCategory ORE_BATHING = REGISTRATE.recipeCategory("ore_bathing",
            GTRecipeTypes.CHEMICAL_BATH_RECIPES);
    public static final GTRecipeCategory CHEM_DYES = REGISTRATE.recipeCategory("chem_dyes",
            GTRecipeTypes.CHEMICAL_BATH_RECIPES);
    public static final GTRecipeCategory INGOT_MOLDING = REGISTRATE.recipeCategory("ingot_molding",
            GTRecipeTypes.ALLOY_SMELTER_RECIPES);

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = REGISTRATE.recipeCategory("arc_furnace_recycling",
            GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(new CategoryIcon(GTCEu.id("textures/gui/icon/category/arc_furnace_recycling.png")));

    public static final GTRecipeCategory MACERATOR_RECYCLING = REGISTRATE.recipeCategory("macerator_recycling",
            GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(new CategoryIcon(GTCEu.id("textures/gui/icon/category/macerator_recycling.png")));

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = REGISTRATE.recipeCategory("extractor_recycling",
            GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(new CategoryIcon(GTCEu.id("textures/gui/icon/category/extractor_recycling.png")));

    public static void init() {}

    public static GTRecipeCategory get(String name) {
        return GTRegistries.RECIPE_CATEGORIES.get(GTCEu.id(name));
    }
}
