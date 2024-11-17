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

    private GTRecipeCategories() {}
}
