package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Accessors(chain = true, fluent = true)
public class EquipmentFoundryRecipeBuilder {

    @Setter
    private @Nullable ResourceLocation id;
    @Setter
    private Ingredient equipment;
    @Getter
    private final Ingredient[] ingredients = new Ingredient[GTValues.TIER_COUNT];
    @Getter
    private final Holder<ItemModule>[] modules = new ItemModule[GTValues.TIER_COUNT];

    public EquipmentFoundryRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public EquipmentFoundryRecipeBuilder tier(int tier, Ingredient ingredient, Holder<ItemModule> module) {
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        ingredient(tier, ingredient);
        module(tier, module);
        return this;
    }

    public EquipmentFoundryRecipeBuilder ingredient(int tier, Ingredient ingredient) {
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        ingredients[tier] = ingredient;
        return this;
    }

    public EquipmentFoundryRecipeBuilder module(int tier, Holder<ItemModule> moduleType) {
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        modules[tier] = moduleType;
        return this;
    }

    public EquipmentFoundryRecipeBuilder ingredient(Ingredient ingredient) {
        return ingredient(0, ingredient);
    }

    public EquipmentFoundryRecipeBuilder ingredient(int tier, TagKey<Item> itemTag) {
        return ingredient(tier, Ingredient.of(itemTag));
    }

    public EquipmentFoundryRecipeBuilder ingredient(TagKey<Item> itemTag) {
        return ingredient(0, Ingredient.of(itemTag));
    }

    public EquipmentFoundryRecipeBuilder ingredient(int tier, ItemStack itemStack) {
        return ingredient(tier, Ingredient.of(itemStack));
    }

    public EquipmentFoundryRecipeBuilder ingredient(ItemStack itemStack) {
        return ingredient(0, itemStack);
    }

    @Tolerate
    public EquipmentFoundryRecipeBuilder ingredient(int tier, ItemLike itemLike) {
        return ingredient(0, Ingredient.of(itemLike));
    }

    public EquipmentFoundryRecipeBuilder ingredient(ItemLike itemLike) {
        return ingredient(Ingredient.of(itemLike));
    }

    public EquipmentFoundryRecipeBuilder module(Holder<ItemModule> moduleType) {
        return module(0, moduleType);
    }

    protected ResourceLocation defaultId() {
        return Objects.requireNonNull(Objects.requireNonNull(modules[0]).getKey()).location();
    }

    public void save(RecipeOutput consumer) {
        var finalId = (id == null ? defaultId() : id).withPrefix("equipment_foundry/");
        consumer.accept(finalId, new EquipmentFoundryRecipe(finalId, equipment, ingredients, modules), null);
    }
}
