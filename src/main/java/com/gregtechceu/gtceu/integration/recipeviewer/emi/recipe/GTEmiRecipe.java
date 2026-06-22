package com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class GTEmiRecipe extends ModularUIEmiRecipe {

    final EmiRecipeCategory category;
    final GTRecipe recipe;

    public GTEmiRecipe(GTRecipe recipe, EmiRecipeCategory category) {
        super(recipe.getId(), () -> new GTRecipeViewerWidget(recipe));
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> ingredients = new ObjectArrayList<>();

        var items = recipe.getInputContents(ItemRecipeCapability.CAP);
        var fluids = recipe.getInputContents(FluidRecipeCapability.CAP);

        for (var itemContent : items) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(itemContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    itemContent.maxChance();

            var mapped = ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));

            ingredients.add(EmiStackConverter.ITEM.convertTo(mapped, chance));
        }

        for (var fluidContent : fluids) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(fluidContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    fluidContent.maxChance();

            var mapped = FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));

            ingredients.add(EmiStackConverter.FLUID.convertTo(mapped, chance));
        }

        return ingredients;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ObjectArrayList<>();

        var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
        var fluids = recipe.getOutputContents(FluidRecipeCapability.CAP);

        for (var itemContent : items) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(itemContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    itemContent.maxChance();

            var mapped = ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));

            outputs.add(EmiStack.of(mapped.getStacks().get(0)).setChance(chance));
        }

        for (var fluidContent : fluids) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(fluidContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    fluidContent.maxChance();

            var mapped = FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));
            var fluid = mapped.getStacks().get(0);
            outputs.add(EmiStack.of(fluid.getFluid(), fluid.getAmount()).setChance(chance));
        }

        return outputs;
    }
}
