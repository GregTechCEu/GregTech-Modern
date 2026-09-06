package com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.JeiIngredientHandler;
import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GTRecipeJEICategory extends ModularUIRecipeCategory<GTRecipe> {

    public static final Function<GTRecipeCategory, RecipeType<GTRecipe>> TYPES = Util
            .memoize(c -> new RecipeType<>(c.registryKey, GTRecipe.class));

    private final GTRecipeCategory category;

    public GTRecipeJEICategory(IJeiHelpers helpers, GTRecipeCategory category) {
        super(GTRecipeViewerWidget::new, GTRecipe::getId);
        this.category = category;
    }

    @Override
    public RecipeType<GTRecipe> getRecipeType() {
        return TYPES.apply(category);
    }

    @Override
    public Component getTitle() {
        return Component.translatable(category.getLanguageKey());
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return (IDrawable) category.getIcon().get();
    }

    public int getMaxWidth() {
        return 250;
    }

    public int getMaxHeight() {
        return 250;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, GTRecipe recipe, IFocusGroup focuses) {
        var itemIn = recipe.getInputContents(ItemRecipeCapability.CAP);
        var fluidIn = recipe.getInputContents(FluidRecipeCapability.CAP);
        var itemOut = recipe.getOutputContents(ItemRecipeCapability.CAP);
        var fluidOut = recipe.getOutputContents(FluidRecipeCapability.CAP);

        for (var itemContent : itemIn) {
            var mapped = ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));

            JeiIngredientHandler.toJeiIngredient(mapped).forEach(
                    stack -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                            .addIngredient(VanillaTypes.ITEM_STACK, stack));
        }

        for (var fluidContent : fluidIn) {
            var mapped = FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));

            JeiIngredientHandler.toJeiIngredient(mapped).forEach(
                    stack -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                            .addIngredient(NeoForgeTypes.FLUID_STACK, stack));
        }

        for (var itemContent : itemOut) {
            var mapped = ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));

            JeiIngredientHandler.toJeiIngredient(mapped)
                    .forEach(stack -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                            .addIngredient(VanillaTypes.ITEM_STACK, stack));
        }

        for (var fluidContent : fluidOut) {
            var mapped = FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));

            JeiIngredientHandler.toJeiIngredient(mapped).forEach(
                    stack -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                            .addIngredient(NeoForgeTypes.FLUID_STACK, stack));
        }
    }

    public static void registerRecipes(IRecipeRegistration registration) {
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
            var wrapped = List.copyOf(type.getRecipesInCategory(category));
            registration.addRecipes(TYPES.apply(category), wrapped);
        }
        // run subcategories
        for (GTRecipeCategory subCategory : subCategories) {
            if (!subCategory.shouldRegisterDisplays()) continue;
            var type = subCategory.getRecipeType();
            var wrapped = List.copyOf(type.getRecipesInCategory(subCategory));
            registration.addRecipes(TYPES.apply(subCategory), wrapped);
        }
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (GTRecipeCategory category : type.getCategories()) {
                    if (!category.isXEIVisible() && !GTCEu.isDev()) continue;
                    registration.addRecipeCatalyst(machine.asStack(), machineType(category));
                }
            }
        }
    }

    public static RecipeType<?> machineType(GTRecipeCategory category) {
        if (category == GTRecipeTypes.FURNACE_RECIPES.getCategory()) return RecipeTypes.SMELTING;
        return TYPES.apply(category);
    }
}
