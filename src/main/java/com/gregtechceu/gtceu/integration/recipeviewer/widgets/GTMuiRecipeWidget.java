package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public class GTMuiRecipeWidget extends ParentWidget<GTMuiRecipeWidget> {

    private final GTRecipe recipe;

    public GTMuiRecipeWidget(GTRecipe recipe) {
        this.recipe = recipe;
    }

    private void initializeWidgets() {}
}
