package com.gregtechceu.gtceu.data.recipe.builder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Accessors(chain = true, fluent = true)
public class ShapelessRecipeBuilder {

    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    @Setter
    protected String group;
    @Setter
    private CraftingBookCategory category = CraftingBookCategory.MISC;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    protected Identifier id;

    public ShapelessRecipeBuilder(@Nullable Identifier id) {
        this.id = id;
    }

    public ShapelessRecipeBuilder requires(TagKey<Item> itemStack) {
        return requires(RecipeBuilderUtil.ingredientOf(itemStack));
    }

    public ShapelessRecipeBuilder requires(ItemStack itemStack) {
        requires(RecipeBuilderUtil.ingredientOf(itemStack));
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

    protected Identifier defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public ShapelessRecipe build() {
        return new ShapelessRecipe(new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(this.category, Objects.requireNonNullElse(this.group, "")),
                ItemStackTemplate.fromNonEmptyStack(this.output), this.ingredients);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;

        consumer.accept(RecipeBuilderUtil.recipeKey(recipeId.withPrefix("shapeless/")), build(), null);
    }
}
