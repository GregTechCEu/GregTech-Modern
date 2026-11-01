package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Accessors(chain = true)
public class GTRecipeCategory {

    public final ResourceLocation id;
    private final Supplier<GTRecipeType> recipeType;
    @Nullable
    @Setter
    private CategoryIcon icon = null;
    @Getter
    @Setter
    private boolean isXEIVisible = true;

    public GTRecipeCategory(@NotNull ResourceLocation id, @NotNull Supplier<GTRecipeType> recipeType) {
        this.recipeType = recipeType;
        this.id = id;
    }

    public static GTRecipeCategory registerDefault(@NotNull GTRecipeType recipeType) {
        GTRecipeCategory category = new GTRecipeCategory(recipeType.id, () -> recipeType);
        Registry.register(GTRegistries.RECIPE_CATEGORIES, category.id, category);
        return category;
    }

    public GTRecipeType getRecipeType() {
        return recipeType.get();
    }

    public String getLanguageKey() {
        return id.toLanguageKey("recipe_category");
    }

    public Component getName() {
        return Component.translatable(getLanguageKey());
    }

    public CategoryIcon getIcon() {
        if (icon == null) {
            if (getRecipeType().getIconSupplier() != null) {
                icon = new CategoryIcon(getRecipeType().getIconSupplier().get());
            } else {
                icon = new CategoryIcon(new ItemStack(Items.BARRIER));
            }
        }
        return icon;
    }

    public void addRecipe(GTRecipe recipe) {
        getRecipeType().addToCategoryMap(this, recipe);
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
