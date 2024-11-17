package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.annotation.Nullable;

public class GTRecipeCategory {

    private static final Map<String, GTRecipeCategory> categories = new Object2ObjectOpenHashMap<>();

    @Getter
    private final String modid;
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
                                      @NotNull String translationKey, @NotNull GTRecipeType recipeType) {
        return categories.computeIfAbsent(categoryName,
                (k) -> new GTRecipeCategory(modID, categoryName, translationKey, recipeType));
    }

    public static GTRecipeCategory of(@NotNull GTRecipeType recipeType) {
        return of(GTCEu.MOD_ID, recipeType.registryName.getPath(), recipeType.registryName.toLanguageKey(), recipeType);
    }

    private GTRecipeCategory(@NotNull String modID, @NotNull String categoryName, @NotNull String translationKey,
                             @NotNull GTRecipeType recipeType) {
        this.modid = modID;
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
