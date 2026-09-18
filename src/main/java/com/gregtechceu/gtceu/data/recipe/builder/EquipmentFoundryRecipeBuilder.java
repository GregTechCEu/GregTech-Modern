package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@Accessors(chain = true, fluent = true)
public class EquipmentFoundryRecipeBuilder {

    @Setter
    private @Nullable ResourceLocation id;
    @Setter
    private Ingredient equipment;
    @Getter
    private Ingredient[] ingredients = new Ingredient[GTValues.TIER_COUNT];
    @Getter
    private ItemModule[] modules = new ItemModule[GTValues.TIER_COUNT];

    public EquipmentFoundryRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public EquipmentFoundryRecipeBuilder tier(int tier, Ingredient ingredient, ItemModule module) {
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

    public EquipmentFoundryRecipeBuilder module(int tier, ItemModule moduleType) {
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

    public EquipmentFoundryRecipeBuilder module(ItemModule moduleType) {
        return module(0, moduleType);
    }

    protected ResourceLocation defaultId() {
        return Objects.requireNonNull(modules[0]).getId();
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        var finalId = (id == null ? defaultId() : id).withPrefix("equipment_foundry/");
        consumer.accept(new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject pJson) {
                EquipmentFoundryRecipe.Serializer.toJson(pJson,
                        new EquipmentFoundryRecipe(finalId, equipment, ingredients, modules));
            }

            @Override
            public ResourceLocation getId() {
                return finalId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return GTRecipeTypes.EQUIPMENT_FOUNDRY_SERIALIZER.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}
