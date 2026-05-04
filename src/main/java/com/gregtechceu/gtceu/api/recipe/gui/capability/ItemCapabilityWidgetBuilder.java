package com.gregtechceu.gtceu.api.recipe.gui.capability;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import net.minecraft.world.item.ItemStack;

public class ItemCapabilityWidgetBuilder implements CapabilityWidgetBuilder<RecipeViewerSlotWidget<?>> {

    public static final ItemCapabilityWidgetBuilder INSTANCE = new ItemCapabilityWidgetBuilder();

    @Override
    public RecipeViewerSlotWidget<?> buildDefaultWidget() {
        return RecipeViewerSlotWidget.create().value(ItemStackList.of(ItemStack.EMPTY));
    }

    @Override
    public void buildWidgetContent(RecipeViewerSlotWidget<?> widget, Content content, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier) {

    }
}
