package com.gregtechceu.gtceu.data.recipe.builder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote SmeltingRecipeBuilder
 */
@Accessors(chain = true, fluent = true)
public class ShapelessRecipeBuilder {

    private NonNullList<Ingredient> ingredients = NonNullList.create();
    @Setter
    protected String group;
    @Setter
    private CraftingBookCategory category = CraftingBookCategory.MISC;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    protected ResourceLocation id;

    public ShapelessRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public ShapelessRecipeBuilder requires(TagKey<Item> itemStack) {
        return requires(Ingredient.of(itemStack));
    }

    public ShapelessRecipeBuilder requires(ItemStack itemStack) {
        if (!itemStack.getComponents().isEmpty()) {
            requires(DataComponentIngredient.of(true, itemStack));
        } else {
            requires(Ingredient.of(itemStack));
        }
        return this;
    }

    public ShapelessRecipeBuilder requires(ItemLike itemLike) {
        return requires(Ingredient.of(itemLike));
    }

    public ShapelessRecipeBuilder requires(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public ShapelessRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public ShapelessRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public ShapelessRecipe build() {
        return new ShapelessRecipe(Objects.requireNonNullElse(this.group, ""), this.category, this.output,
                this.ingredients);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;

        consumer.accept(ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), "shapeless" + "/" + recipeId.getPath()), build(),
                null);
    }
}
