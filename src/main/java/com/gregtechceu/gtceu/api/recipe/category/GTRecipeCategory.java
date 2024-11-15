package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.annotation.Nullable;

public class GTRecipeCategory {

    private static final Map<String, GTRecipeCategory> categories = new Object2ObjectOpenHashMap<>();
    public static final GTRecipeCategory EMPTY = new GTRecipeCategory();

    @Getter
    private final String modID;
    @Getter
    private final String name;
    @Getter
    private final String uniqueID;
    @Getter
    private final String translation;
    @Getter
    private final GTRecipeType recipeType;
    @Nullable
    @Getter
    private Object icon = null;
    @Getter
    private ResourceLocation resourceLocation;

    public static GTRecipeCategory of(@NotNull String modID, @NotNull String categoryName,
                                      @NotNull GTRecipeType recipeType, @NotNull String translationKey) {
        return categories.computeIfAbsent(categoryName,
                (k) -> new GTRecipeCategory(modID, categoryName, recipeType, translationKey));
    }

    public static GTRecipeCategory of(@NotNull String modID, @NotNull String categoryName,
                                      @NotNull GTRecipeType recipeType) {
        return of(modID, categoryName, recipeType, "%s.recipe.category.%s".formatted(modID, categoryName));
    }

    public static GTRecipeCategory of(@NotNull GTRecipeType recipeType) {
        return of(GTCEu.MOD_ID, recipeType.registryName.getPath(), recipeType, recipeType.registryName.toLanguageKey());
    }

    private GTRecipeCategory() {
        this.modID = "";
        this.name = "";
        this.uniqueID = "";
        this.translation = "";
        this.recipeType = GTRecipeTypes.DUMMY_RECIPES;
    }

    private GTRecipeCategory(@NotNull String modID, @NotNull String categoryName, @NotNull GTRecipeType recipeType,
                             @NotNull String translationKey) {
        this.modID = modID;
        this.name = categoryName;
        this.uniqueID = modID + ":" + this.name;
        this.translation = translationKey;
        this.recipeType = recipeType;
        this.resourceLocation = new ResourceLocation(modID, categoryName);
        GTRegistries.RECIPE_CATEGORIES.register(resourceLocation, this);
    }

    public GTRecipeCategory setIcon(@Nullable Object icon) {
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

        return getUniqueID().equals(that.getUniqueID());
    }

    @Override
    public int hashCode() {
        return getUniqueID().hashCode();
    }

    @Override
    public String toString() {
        return "GTRecipeCategory{" + uniqueID + "}";
    }
}
