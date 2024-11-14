package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;

public class GTRecipeCategories {

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "arc_furnace_recycling",
                    "gtceu.recipe.category.arc_furnace_recycling",
                    GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "macerator_recycling",
                    "gtceu.recipe.category.macerator_recycling",
                    GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(GuiTextures.MACERATOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "extractor_recycling",
                    "gtceu.recipe.category.extractor_recycling",
                    GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(GuiTextures.EXTRACTOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory ORE_CRUSHING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ore_crushing",
                    "gtceu.recipe.category.ore_crushing",
                    GTRecipeTypes.MACERATOR_RECIPES);

    public static final GTRecipeCategory ORE_FORGING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ore_forging",
                    "gtceu.recipe.category.ore_forging",
                    GTRecipeTypes.FORGE_HAMMER_RECIPES);

    public static final GTRecipeCategory ORE_BATHING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ore_bathing",
                    "gtceu.recipe.category.ore_bathing",
                    GTRecipeTypes.CHEMICAL_BATH_RECIPES);

    public static final GTRecipeCategory CHEM_DYES = GTRecipeCategory
            .of(GTCEu.MOD_ID, "chem_dyes",
                    "gtceu.recipe.category.chem_dyes",
                    GTRecipeTypes.CHEMICAL_BATH_RECIPES);

    public static final GTRecipeCategory INGOT_MOLDING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ingot_molding",
                    "gtceu.recipe.category.ingot_molding",
                    GTRecipeTypes.ALLOY_SMELTER_RECIPES);

    private GTRecipeCategories() {}
}
