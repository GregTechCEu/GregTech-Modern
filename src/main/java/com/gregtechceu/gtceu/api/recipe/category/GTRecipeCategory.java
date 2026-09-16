package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true)
public class GTRecipeCategory {

    public final ResourceLocation id;
    @Getter
    private final GTRecipeType recipeType;
    @Nullable
    @Setter
    private CategoryIcon icon = null;
    @Getter
    @Setter
    private boolean isXEIVisible = true;

    public GTRecipeCategory(@NotNull ResourceLocation id, @NotNull GTRecipeType recipeType) {
        this.recipeType = recipeType;
        this.id = id;
    }

    public static GTRecipeCategory registerDefault(@NotNull GTRecipeType recipeType) {
        GTRecipeCategory category = new GTRecipeCategory(recipeType.id, recipeType);
        Registry.register(GTRegistries.RECIPE_CATEGORIES, category.id, category);
        return category;
    }

    public String getLanguageKey() {
        return id.toLanguageKey("recipe_category");
    }

    public Component getName() {
        return Component.translatable(getLanguageKey());
    }

    public CategoryIcon getIcon() {
        if (icon == null) {
            if (recipeType.getIconSupplier() != null) {
                icon = new CategoryIcon(recipeType.getIconSupplier().get());
            } else {
                icon = new CategoryIcon(new ItemStack(Items.BARRIER));
            }
        }
        return icon;
    }

    public void addRecipe(GTRecipe recipe) {
        recipeType.addToCategoryMap(this, recipe);
    }

    public boolean shouldRegisterDisplays() {
        return (isXEIVisible || GTCEu.isDev()) &&
                this != GTRecipeTypes.FURNACE_RECIPES.getCategory();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeCategory that)) return false;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GTRecipeCategory{%s}".formatted(this.id);
    }
}
