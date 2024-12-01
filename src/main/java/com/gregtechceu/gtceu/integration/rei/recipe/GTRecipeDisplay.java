package com.gregtechceu.gtceu.integration.rei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

import net.minecraft.resources.ResourceLocation;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.Optional;

public class GTRecipeDisplay extends ModularDisplay<WidgetGroup> {

    private final GTRecipe recipe;

    public GTRecipeDisplay(GTRecipe recipe, CategoryIdentifier<?> category) {
        super(() -> new GTRecipeWidget(recipe), category);
        this.recipe = recipe;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.id);
    }
}
