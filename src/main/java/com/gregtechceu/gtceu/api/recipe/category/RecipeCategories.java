package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

public class RecipeCategories {

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = GTRecipeCategory
            .create(GTCEu.MOD_ID, "arc_furnace_recycling",
                    "gregtech.recipe.category.arc_furnace_recycling",
                    GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory
            .create(GTCEu.MOD_ID, "macerator_recycling",
                    "gregtech.recipe.category.macerator_recycling",
                    GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = GTRecipeCategory
            .create(GTCEu.MOD_ID, "extractor_recycling",
                    "gregtech.recipe.category.extractor_recycling",
                    GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    private RecipeCategories() {}
}
