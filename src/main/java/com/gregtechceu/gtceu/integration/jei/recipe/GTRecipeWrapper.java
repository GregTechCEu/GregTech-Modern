package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.world.item.crafting.RecipeHolder;

public class GTRecipeWrapper extends ModularWrapper<Widget> {

    public final RecipeHolder<GTRecipe> recipe;

    public GTRecipeWrapper(RecipeHolder<GTRecipe> recipe) {
        super(new GTRecipeWidget(recipe));
        this.recipe = recipe;
    }
}
