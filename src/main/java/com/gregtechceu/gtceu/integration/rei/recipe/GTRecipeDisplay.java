package com.gregtechceu.gtceu.integration.rei.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

import java.util.Optional;

public class GTRecipeDisplay extends ModularDisplay<WidgetGroup> {

    @Getter
    private final GTRecipeREICategory category;
    private final GTRecipe recipe;

    public GTRecipeDisplay(GTRecipeREICategory category, GTRecipe recipe) {
        super(() -> new GTRecipeWidget(recipe), GTRecipeREICategory.CATEGORIES.apply(recipe.recipeCategory));
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.id);
    }
}
