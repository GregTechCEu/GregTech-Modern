package com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ModuleRecipeWidget;
import net.minecraft.world.item.crafting.Ingredient;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public class GTModuleEMIRecipe extends ModularUIEmiRecipe implements EmiRecipe {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            GTCEu.id("equipment_foundry"),
            EmiIngredient.of(Ingredient.of(GTBlocks.EQUIPMENT_FOUNDRY)));

    private final EquipmentFoundryRecipe recipe;

    public GTModuleEMIRecipe(EquipmentFoundryRecipe recipe) {
        super(recipe.getId(), () -> new ModuleRecipeWidget(recipe));
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(
                EmiIngredient.of(recipe.getEquipment()),
                EmiIngredient.of(recipe.getIngredient())
        );
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(EmiIngredient.of(Ingredient.of(GTBlocks.EQUIPMENT_FOUNDRY)));
    }

    public static void addRecipes(EmiRegistry registry) {
        registry.getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get())
                .stream()
                .map(GTModuleEMIRecipe::new)
                .forEach(registry::addRecipe);
    }
}
