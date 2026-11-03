package com.gregtechceu.gtceu.api.recipe.category;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

@Accessors(chain = true)
public class GTRecipeCategory {

    @ApiStatus.Internal
    public static Map<ResourceKey<RecipeType<?>>, Supplier<ItemStack>> ICON_CACHE = new Object2ObjectOpenHashMap<>();

    public final ResourceLocation id;
    private final Supplier<GTRecipeType> recipeType;
    @Nullable
    @Setter
    private CategoryIcon icon = null;
    @Getter
    @Setter
    private boolean isXEIVisible = true;
    @Getter
    private final boolean isDefault;

    private GTRecipeCategory(@NotNull ResourceLocation id, @NotNull Supplier<GTRecipeType> recipeType,
                             boolean isDefault) {
        this.recipeType = recipeType;
        this.id = id;
        this.isDefault = isDefault;
    }

    public GTRecipeCategory(@NotNull ResourceLocation id, @NotNull Supplier<GTRecipeType> recipeType) {
        this(id, recipeType, false);
    }

    public static GTRecipeCategory registerDefault(@NotNull GTRecipeType recipeType) {
        GTRecipeCategory category = new GTRecipeCategory(recipeType.id, () -> recipeType, true);
        Registry.register(GTRegistries.RECIPE_CATEGORIES, category.id, category);
        return category;
    }

    public GTRecipeType getRecipeType() {
        return recipeType.get();
    }

    public String getLanguageKey() {
        return isDefault ? id.toLanguageKey("recipe_type") : id.toLanguageKey("recipe_category");
    }

    public Component getName() {
        return Component.translatable(getLanguageKey());
    }

    public CategoryIcon getIcon() {
        if (icon == null) {
            var defaultKey = ResourceKey.create(Registries.RECIPE_TYPE, recipeType.get().id);
            if (getRecipeType().getIconSupplier() != null) {
                icon = new CategoryIcon(getRecipeType().getIconSupplier().get());
            } else if (ICON_CACHE.containsKey(defaultKey)) {
                icon = new CategoryIcon(ICON_CACHE.get(defaultKey).get());
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
