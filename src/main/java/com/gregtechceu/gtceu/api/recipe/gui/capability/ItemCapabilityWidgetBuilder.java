package com.gregtechceu.gtceu.api.recipe.gui.capability;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;

public class ItemCapabilityWidgetBuilder implements CapabilityWidgetBuilder<RecipeViewerSlotWidget<?>> {

    public static final ItemCapabilityWidgetBuilder INSTANCE = new ItemCapabilityWidgetBuilder();

    @Override
    public RecipeViewerSlotWidget<?> buildDefaultWidget() {
        return RecipeViewerSlotWidget.create().background(GuiTextures.SLOT_ITEM);
    }

    @Override
    public void buildWidgetContent(RecipeViewerSlotWidget<?> widget, Content content, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier) {

    }
}
