package com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.trait.LatheRecipeLogic;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.GTEMIPlugin;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GTRecipeEMICategory extends EmiRecipeCategory {

    public static final Function<GTRecipeCategory, GTRecipeEMICategory> CATEGORIES = Util
            .memoize(GTRecipeEMICategory::new);
    private final GTRecipeCategory category;

    private GTRecipeEMICategory(GTRecipeCategory category) {
        super(category.id, (EmiRenderable) category.getIcon().get());
        this.category = category;
    }

    public static void registerDisplays(EmiRegistry registry) {
        List<GTRecipeCategory> subCategories = new ArrayList<>();
        // run main categories first
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (!category.shouldRegisterDisplays()) continue;
            var type = category.getRecipeType();
            if (category == type.getCategory()) {
                type.buildRepresentativeRecipes();
            } else {
                subCategories.add(category);
                continue;
            }
            EmiRecipeCategory emiCategory = CATEGORIES.apply(category);
            type.getRecipesInCategory(category)
                    .forEach(recipe -> registerDisplay(registry, recipe, emiCategory));
        }
        // run subcategories
        for (var subCategory : subCategories) {
            if (!subCategory.shouldRegisterDisplays()) continue;
            var type = subCategory.getRecipeType();
            EmiRecipeCategory emiCategory = CATEGORIES.apply(subCategory);
            type.getRecipesInCategory(subCategory)
                    .forEach(recipe -> registerDisplay(registry, recipe, emiCategory));
        }
    }

    private static void registerDisplay(EmiRegistry registry, GTRecipe recipe, EmiRecipeCategory category) {
        registry.addRecipe(new GTEmiRecipe(recipe, category));
        var lubricated = LatheRecipeLogic.createLubricatedRecipe(recipe);
        if (lubricated != null) {
            // EMI synthetic recipes require the leading slash, otherwise logs will flood heavily.
            lubricated.id = recipe.id.withPrefix("/").withSuffix("_lubricated");
            registry.addRecipe(new GTEmiRecipe(lubricated, category));
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        for (MachineDefinition machine : GTEMIPlugin.SORTED_MACHINES) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (GTRecipeCategory category : type.getCategories()) {
                    if (!category.isXEIVisible() && !GTCEu.isDev()) continue;
                    registry.addWorkstation(machineCategory(category), EmiStack.of(machine.asStack()));
                }
            }
        }
    }

    public static EmiRecipeCategory machineCategory(GTRecipeCategory category) {
        if (category == GTRecipeTypes.FURNACE_RECIPES.getCategory()) return VanillaEmiRecipeCategories.SMELTING;
        else return CATEGORIES.apply(category);
    }

    @Override
    public Component getName() {
        return Component.translatable(category.getLanguageKey());
    }
}
