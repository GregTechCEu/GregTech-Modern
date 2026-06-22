package com.gregtechceu.gtceu.integration.recipeviewer.rei.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.integration.rei.REIStackConverter;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplay;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTRecipeDisplay extends ModularUIREIDisplay {

    private final GTRecipe recipe;

    public GTRecipeDisplay(GTRecipe recipe, CategoryIdentifier<?> category) {
        super(recipe.id, () -> new GTRecipeViewerWidget(recipe), category);
        this.recipe = recipe;
    }

    private List<EntryIngredient> getInputs() {
        List<EntryIngredient> ingredients = new ObjectArrayList<>();

        var items = recipe.getInputContents(ItemRecipeCapability.CAP);
        var fluids = recipe.getInputContents(FluidRecipeCapability.CAP);

        for (var itemContent : items) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(itemContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    itemContent.maxChance();

            var mapped = ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));

            ingredients.add(REIStackConverter.ITEM.convertTo(mapped, chance, $ -> $));
        }

        for (var fluidContent : fluids) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(fluidContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    fluidContent.maxChance();

            var mapped = FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));

            ingredients.add(REIStackConverter.FLUID.convertTo(mapped, chance, $ -> $));
        }

        return ingredients;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.id);
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return getInputs();
    }

    @Override
    public List<EntryIngredient> getRequiredEntries() {
        return getInputEntries();
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> outputs = new ObjectArrayList<>();

        var items = recipe.getOutputContents(ItemRecipeCapability.CAP);
        var fluids = recipe.getOutputContents(FluidRecipeCapability.CAP);

        for (var itemContent : items) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(itemContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    itemContent.maxChance();

            var mapped = ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));

            var entryStack = EntryStack.of(VanillaEntryTypes.ITEM, mapped.getStacks().get(0));
            outputs.add(EntryIngredient.of(entryStack));
        }

        for (var fluidContent : fluids) {
            float chance = (float) recipe.recipeType.getChanceFunction()
                    .getBoostedChance(fluidContent, RecipeHelper.getRecipeEUtTier(recipe),
                            RecipeHelper.getRecipeEUtTier(recipe)) /
                    fluidContent.maxChance();

            var mapped = FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));
            var fluid = mapped.getStacks().get(0);
            var entryStack = EntryStack.of(VanillaEntryTypes.FLUID,
                    dev.architectury.fluid.FluidStack.create(fluid.getFluid(), fluid.getAmount(), fluid.getTag()));
            outputs.add(EntryIngredient.of(entryStack));
        }

        return outputs;
    }
}
