package com.gregtechceu.gtceu.data.recipe.builder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

public final class RecipeBuilderUtil {

    private RecipeBuilderUtil() {}

    public static Ingredient ingredientOf(TagKey<Item> tag) {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag));
    }

    public static Ingredient ingredientOf(ItemStack stack) {
        if (!stack.isComponentsPatchEmpty()) {
            return DataComponentIngredient.of(true, stack);
        }
        return Ingredient.of(stack.getItem());
    }

    public static ResourceKey<Recipe<?>> recipeKey(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }
}
