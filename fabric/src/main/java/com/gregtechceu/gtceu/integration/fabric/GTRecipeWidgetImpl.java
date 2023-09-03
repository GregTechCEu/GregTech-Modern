package com.gregtechceu.gtceu.integration.fabric;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

public class GTRecipeWidgetImpl {
    public static void collectExtraStorage(Table<IO, RecipeCapability<?>, Object> extraTable, Table<IO, RecipeCapability<?>, List<Content>> extraContents, GTRecipe recipe) {

    }

    public static void renderExtras(GTRecipe recipe, WidgetGroup group, Table<IO, RecipeCapability<?>, List<Content>> extraContents) {
    }

}
