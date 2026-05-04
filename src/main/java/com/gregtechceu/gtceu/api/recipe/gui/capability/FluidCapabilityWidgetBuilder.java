package com.gregtechceu.gtceu.api.recipe.gui.capability;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import net.minecraftforge.fluids.FluidStack;

public class FluidCapabilityWidgetBuilder implements CapabilityWidgetBuilder<RecipeViewerSlotWidget<?>> {

    public static final FluidCapabilityWidgetBuilder INSTANCE = new FluidCapabilityWidgetBuilder();

    @Override
    public RecipeViewerSlotWidget<?> buildDefaultWidget() {
        return RecipeViewerSlotWidget.create().value(FluidStackList.of(FluidStack.EMPTY));
    }

    @Override
    public void buildWidgetContent(RecipeViewerSlotWidget<?> widget, Content content, GTRecipeType recipeType, GTRecipe recipe, int recipeTier, int chanceTier) {

    }
}
