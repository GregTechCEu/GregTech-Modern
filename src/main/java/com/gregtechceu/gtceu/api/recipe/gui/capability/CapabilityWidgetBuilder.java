package com.gregtechceu.gtceu.api.recipe.gui.capability;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widget.Widget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;

public interface CapabilityWidgetBuilder<T extends Widget<?>> {

    T buildDefaultWidget();

    void buildWidgetContent(T widget, Content content, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier);
}
