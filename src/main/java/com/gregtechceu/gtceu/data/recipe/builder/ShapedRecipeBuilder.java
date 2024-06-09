package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.StrictShapedRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote ShapedRecipeBuilder
 */
@Accessors(chain = true, fluent = true)
public class ShapedRecipeBuilder {

    @Setter
    protected ItemStack output = ItemStack.EMPTY;
    @Setter
    protected ResourceLocation id;
    @Setter
    protected String group;
    @Setter
    private RecipeCategory category = RecipeCategory.MISC;

    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    @Setter
    protected boolean isStrict;

    public ShapedRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public ShapedRecipeBuilder() {
        this(null);
    }

    public ShapedRecipeBuilder pattern(String slice) {
        rows.add(slice);
        return this;
    }

    public ShapedRecipeBuilder define(char cha, TagKey<Item> itemStack) {
        key.put(cha, Ingredient.of(itemStack));
        return this;
    }

    public ShapedRecipeBuilder define(char cha, ItemStack itemStack) {
        if (!itemStack.getComponents().isEmpty()) {
            key.put(cha, DataComponentIngredient.of(true, itemStack));
        } else {
            key.put(cha, Ingredient.of(itemStack));
        }
        return this;
    }

    public ShapedRecipeBuilder define(char cha, ItemLike itemLike) {
        key.put(cha, Ingredient.of(itemLike));
        return this;
    }

    public ShapedRecipeBuilder define(char cha, Ingredient ingredient) {
        key.put(cha, ingredient);
        return this;
    }

    public ShapedRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        ShapedRecipe shapedrecipe = isStrict ? new StrictShapedRecipe(
                Objects.requireNonNullElse(this.group, ""),
                RecipeBuilder.determineBookCategory(this.category),
                ShapedRecipePattern.of(key, rows),
                this.output,
                false) :
                new ShapedRecipe(
                        Objects.requireNonNullElse(this.group, ""),
                        RecipeBuilder.determineBookCategory(this.category),
                        ShapedRecipePattern.of(key, rows),
                        this.output,
                        false);
        consumer.accept(ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), "shaped/" + recipeId.getPath()), shapedrecipe,
                null);
    }
}
