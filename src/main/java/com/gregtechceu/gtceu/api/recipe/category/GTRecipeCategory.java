package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GTRecipeCategory {

    // Placeholder category used if category isn't defined for a recipe for registration
    public static final GTRecipeCategory DEFAULT = new GTRecipeCategory("default", GTRecipeTypes.DUMMY_RECIPES);

    public final ResourceLocation registryKey;
    public final String name;
    @Getter
    private final GTRecipeType recipeType;
    @Getter
    private final String languageKey;
    @Nullable
    @Getter
    private IGuiTexture icon = null;

    public GTRecipeCategory(@NotNull GTRecipeType recipeType) {
        this.recipeType = recipeType;
        this.name = recipeType.registryName.getPath();
        this.registryKey = recipeType.registryName;
        this.languageKey = recipeType.registryName.toLanguageKey();
    }

    public GTRecipeCategory(@NotNull String categoryName, @NotNull GTRecipeType recipeType) {
        this.recipeType = recipeType;
        this.name = categoryName;
        this.registryKey = GTCEu.id(categoryName);
        this.languageKey = "%s.recipe.category.%s".formatted(GTCEu.MOD_ID, categoryName);
    }

    public static GTRecipeCategory registerDefault(@NotNull GTRecipeType recipeType) {
        GTRecipeCategory category = new GTRecipeCategory(recipeType);
        GTRegistries.RECIPE_CATEGORIES.register(category.registryKey, category);
        return category;
    }

    public GTRecipeCategory setIcon(@Nullable IGuiTexture icon) {
        this.icon = icon;
        return this;
    }

    public boolean isXEIVisible() {
        return recipeType.getRecipeUI().isXEIVisible();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GTRecipeCategory that = (GTRecipeCategory) obj;
        return registryKey.equals(that.registryKey);
    }

    @Override
    public int hashCode() {
        return registryKey.hashCode();
    }

    @Override
    public String toString() {
        return "GTRecipeCategory{%s}".formatted(name);
    }
}
