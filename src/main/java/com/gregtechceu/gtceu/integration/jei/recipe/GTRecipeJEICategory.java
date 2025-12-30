package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.jei.IGui2IDrawable;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GTRecipeJEICategory extends ModularUIRecipeCategory<GTRecipe> {

    public static final Function<GTRecipeCategory, RecipeType<GTRecipe>> TYPES = Util
            .memoize(c -> new RecipeType<>(c.registryKey, GTRecipe.class));

    private final GTRecipeCategory category;
    @Getter
    private final IDrawable background;
    @Getter
    private final IDrawable icon;

    public GTRecipeJEICategory(IJeiHelpers helpers,
                               @NotNull GTRecipeCategory category) {
        super(GTRecipeWrapper::new);
        this.category = category;
        var recipeType = category.getRecipeType();
        IGuiHelper guiHelper = helpers.getGuiHelper();
        var size = recipeType.getRecipeUI().getJEISize();
        this.background = guiHelper.createBlankDrawable(size.width, size.height);
        this.icon = IGui2IDrawable.toDrawable(category.getIcon(), 16, 16);
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

    @Override
    @NotNull
    public RecipeType<GTRecipe> getRecipeType() {
        return TYPES.apply(category);
    }

    @Override
    @NotNull
    public Component getTitle() {
        return Component.translatable(category.getLanguageKey());
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull GTRecipe recipe) {
        return recipe.id;
    }
}
