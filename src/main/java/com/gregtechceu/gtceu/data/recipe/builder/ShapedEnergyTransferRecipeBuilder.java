package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Accessors(fluent = true, chain = true)
public class ShapedEnergyTransferRecipeBuilder {

    @Setter
    protected Ingredient chargeIngredient;
    @Setter
    protected ItemStack output = ItemStack.EMPTY;
    @Setter
    protected Identifier id;
    @Setter
    protected String group;
    @Setter
    private CraftingBookCategory category = CraftingBookCategory.MISC;
    @Setter
    protected boolean transferMaxCharge;
    @Setter
    protected boolean overrideCharge;

    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();

    public ShapedEnergyTransferRecipeBuilder(@Nullable Identifier id) {
        this.id = id;
    }

    public ShapedEnergyTransferRecipeBuilder() {
        this(null);
    }

    public ShapedEnergyTransferRecipeBuilder pattern(String slice) {
        rows.add(slice);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, TagKey<Item> itemStack) {
        key.put(cha, RecipeBuilderUtil.ingredientOf(itemStack));
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, ItemStack itemStack) {
        key.put(cha, RecipeBuilderUtil.ingredientOf(itemStack));
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, ItemLike itemLike) {
        key.put(cha, Ingredient.of(itemLike));
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, Ingredient ingredient) {
        key.put(cha, ingredient);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    public ShapedEnergyTransferRecipe build() {
        return new ShapedEnergyTransferRecipe(Objects.requireNonNullElse(this.group, ""), this.category,
                ShapedRecipePattern.of(this.key, this.rows), Objects.requireNonNull(this.chargeIngredient),
                this.overrideCharge, this.transferMaxCharge, this.output, false);
    }

    protected Identifier defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        consumer.accept(RecipeBuilderUtil.recipeKey(recipeId.withPrefix("shaped/")), build(), null);
    }
}
