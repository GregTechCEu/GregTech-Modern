package com.gregtechceu.gtceu.integration.rei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

public class GTRecipeDisplay extends ModularDisplay<WidgetGroup> {

    public GTRecipeDisplay(GTRecipe recipe) {
        super(() -> new GTRecipeWidget(recipe), GTRecipeTypeDisplayCategory.CATEGORIES.apply(recipe.recipeType));
    }
}
