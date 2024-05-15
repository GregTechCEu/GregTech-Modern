package com.gregtechceu.gtceu.data.recipe.builder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
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
public class BlastingRecipeBuilder {

    private Ingredient input;
    @Setter
    protected String group;
    @Setter
    protected CookingBookCategory category = CookingBookCategory.MISC;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    private float experience;
    @Setter
    private int cookingTime;
    @Setter
    protected ResourceLocation id;

    public BlastingRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public BlastingRecipeBuilder input(TagKey<Item> itemStack) {
        return input(Ingredient.of(itemStack));
    }

    public BlastingRecipeBuilder input(ItemStack itemStack) {
        if (!itemStack.getComponents().isEmpty()) {
            input = DataComponentIngredient.of(true, itemStack);
        } else {
            input = Ingredient.of(itemStack);
        }
        return this;
    }

    public BlastingRecipeBuilder input(ItemLike itemLike) {
        return input(Ingredient.of(itemLike));
    }

    public BlastingRecipeBuilder input(Ingredient ingredient) {
        input = ingredient;
        return this;
    }

    public BlastingRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public BlastingRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    private BlastingRecipe create() {
        return new BlastingRecipe(Objects.requireNonNullElse(this.group, ""), this.category, this.input, this.output,
                this.experience, this.cookingTime);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        consumer.accept(new ResourceLocation(recipeId.getNamespace(), "blasting/" + recipeId.getPath()), create(),
                null);
    }
}
