package com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTRecipeWrapper extends ModularWrapper<Widget> {

    public final GTRecipe recipe;

    public GTRecipeWrapper(GTRecipe recipe) {
        super(new WidgetGroup());
        this.recipe = recipe;
    }
}
