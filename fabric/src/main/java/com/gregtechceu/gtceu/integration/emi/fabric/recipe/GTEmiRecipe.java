package com.gregtechceu.gtceu.integration.emi.fabric.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.gregtechceu.gtlib.emi.ModularEmiRecipe;
import com.gregtechceu.gtlib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GTEmiRecipe extends ModularEmiRecipe<WidgetGroup> {
    final GTRecipeTypeEmiCategory category;
    final GTRecipe recipe;

    public GTEmiRecipe(GTRecipeTypeEmiCategory category, GTRecipe recipe) {
        super(() -> new GTRecipeWidget(recipe));
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }
}
