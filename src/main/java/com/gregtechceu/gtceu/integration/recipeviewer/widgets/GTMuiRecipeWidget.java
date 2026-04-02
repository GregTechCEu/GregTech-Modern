package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import brachy.modularui.widget.ParentWidget;

public class GTMuiRecipeWidget extends ParentWidget<GTMuiRecipeWidget> {

    private final GTRecipe recipe;

    public GTMuiRecipeWidget(GTRecipe recipe) {
        this.recipe = recipe;
    }

    private void initializeWidgets() {}
}
