package com.gregtechceu.gtceu.integration.rei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import com.gregtechceu.gtlib.rei.ModularDisplay;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class GTRecipeDisplay extends ModularDisplay<WidgetGroup> {

    private final GTRecipe recipe;

    public GTRecipeDisplay(GTRecipe recipe) {
        super(() -> new GTRecipeWidget(recipe), GTRecipeTypeDisplayCategory.CATEGORIES.apply(recipe.recipeType));
        this.recipe = recipe;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.id);
    }
}
