package com.gregtechceu.gtceu.integration.recipeviewer.rei.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import brachy.modularui.integration.rei.recipe.ModularUIREIDisplayCategory;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GTRecipeREICategory extends ModularUIREIDisplayCategory<GTRecipeDisplay> {

    public static final Function<Holder<GTRecipeCategory>, CategoryIdentifier<GTRecipeDisplay>> CATEGORIES = Util
            .memoize(c -> CategoryIdentifier.of(c.unwrapKey().get().location()));

    private final Holder<GTRecipeCategory> category;
    @Getter
    private final Renderer icon;

    public GTRecipeREICategory(@NotNull Holder<GTRecipeCategory> category) {
        this.category = category;
        this.icon = (Renderer) category.value().getIcon().get();
    }

    public static void registerDisplays(DisplayRegistry registry) {
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
            var identifier = CATEGORIES.apply(category.getHolder());
            type.getRecipesInCategory(category).stream()
                    .map(r -> new GTRecipeDisplay(r, identifier))
                    .forEach(registry::add);
        }
        // run subcategories
        for (GTRecipeCategory subCategory : subCategories) {
            if (!subCategory.shouldRegisterDisplays()) continue;
            var type = subCategory.getRecipeType();
            var identifier = CATEGORIES.apply(subCategory.getHolder());
            type.getRecipesInCategory(subCategory).stream()
                    .map(r -> new GTRecipeDisplay(r, identifier))
                    .forEach(registry::add);
        }
    }

    public static void registerWorkStations(CategoryRegistry registry) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (Holder<GTRecipeCategory> category : type.getCategories()) {
                    if (!category.value().isXEIVisible() && !GTCEu.isDev()) continue;
                    registry.addWorkstations(machineCategory(category), EntryStacks.of(machine.asStack()));
                }
            }
        }
    }

    public static CategoryIdentifier<?> machineCategory(Holder<GTRecipeCategory> category) {
        if (category == GTRecipeTypes.FURNACE_RECIPES.getCategory()) return BuiltinPlugin.SMELTING;
        else return CATEGORIES.apply(category);
    }

    @Override
    public CategoryIdentifier<? extends GTRecipeDisplay> getCategoryIdentifier() {
        return CATEGORIES.apply(category);
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable(category.value().getLanguageKey());
    }
}
