package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtlib.gui.widget.Widget;
import com.gregtechceu.gtlib.jei.ModularWrapper;

public class GTRecipeWrapper extends ModularWrapper<Widget> {

    public final GTRecipe recipe;

    public GTRecipeWrapper(GTRecipe recipe) {
        super(new GTRecipeWidget(recipe));
        this.recipe = recipe;
    }

}
