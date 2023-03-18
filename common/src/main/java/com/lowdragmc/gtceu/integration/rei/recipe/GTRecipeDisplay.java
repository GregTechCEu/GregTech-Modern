package com.lowdragmc.gtceu.integration.rei.recipe;

import com.lowdragmc.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.gtceu.integration.GTRecipeWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

public class GTRecipeDisplay extends ModularDisplay<WidgetGroup> {

    public GTRecipeDisplay(GTRecipeType recipeMap, GTRecipe recipe) {
        super(() -> new GTRecipeWidget(recipe), GTRecipeTypeDisplayCategory.CATEGORIES.apply(recipeMap));
    }
}
