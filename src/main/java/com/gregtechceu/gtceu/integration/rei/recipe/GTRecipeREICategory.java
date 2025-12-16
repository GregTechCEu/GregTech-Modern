package com.gregtechceu.gtceu.integration.rei.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.rei.IGui2Renderer;
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

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

public class GTRecipeREICategory extends ModularUIDisplayCategory<GTRecipeDisplay> {

    public static final Function<GTRecipeCategory, CategoryIdentifier<GTRecipeDisplay>> CATEGORIES = Util
            .memoize(c -> CategoryIdentifier.of(c.registryKey));

    private final GTRecipeCategory category;
    @Getter
    private final Renderer icon;
    @Getter
    private final Size size;

    public GTRecipeREICategory(@NotNull GTRecipeCategory category) {
        this.category = category;
        var recipeType = category.getRecipeType();
        var size = recipeType.getRecipeUI().getJEISize();
        this.size = new Size(size.width + 8, size.height + 8);
        this.icon = IGui2Renderer.toDrawable(category.getIcon());
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
            var identifier = CATEGORIES.apply(category);
            type.getRecipesInCategory(category).stream()
                    .map(r -> new GTRecipeDisplay(r, identifier))
                    .forEach(registry::add);
        }
        // run subcategories
        for (GTRecipeCategory subCategory : subCategories) {
            if (!subCategory.shouldRegisterDisplays()) continue;
            var type = subCategory.getRecipeType();
            var identifier = CATEGORIES.apply(subCategory);
            type.getRecipesInCategory(subCategory).stream()
                    .map(r -> new GTRecipeDisplay(r, identifier))
                    .forEach(registry::add);
        }
    }

    public static void registerWorkStations(CategoryRegistry registry) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (GTRecipeCategory category : type.getCategories()) {
                    if (!category.isXEIVisible() && !GTCEu.isDev()) continue;
                    registry.addWorkstations(machineCategory(category), EntryStacks.of(machine.asStack()));
                }
            }
        }
    }

    public static CategoryIdentifier<?> machineCategory(GTRecipeCategory category) {
        if (category == GTRecipeTypes.FURNACE_RECIPES.getCategory()) return BuiltinPlugin.SMELTING;
        else return CATEGORIES.apply(category);
    }

    @Override
    public CategoryIdentifier<? extends GTRecipeDisplay> getCategoryIdentifier() {
        return CATEGORIES.apply(category);
    }

    @Override
    public int getDisplayHeight() {
        return getSize().height;
    }

    @Override
    public int getDisplayWidth(GTRecipeDisplay display) {
        return getSize().width;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable(category.getLanguageKey());
    }
}
