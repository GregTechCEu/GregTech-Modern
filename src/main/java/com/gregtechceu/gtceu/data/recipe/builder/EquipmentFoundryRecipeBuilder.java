package com.gregtechceu.gtceu.data.recipe.builder;

import com.google.common.base.Preconditions;
import com.google.gson.JsonNull;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import lombok.Getter;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

@Accessors(chain = true, fluent = true)
public class EquipmentFoundryRecipeBuilder {

    @Setter
    private @Nullable ResourceLocation id;
    @Setter
    private Ingredient equipment;
    @Getter
    private Ingredient[] ingredients = new Ingredient[0];
    @Getter
    private ItemModuleType<?>[] modules = new ItemModuleType[0];

    public EquipmentFoundryRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public EquipmentFoundryRecipeBuilder tier(int tier, Ingredient ingredient, ItemModuleType<?> module) {
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        ingredient(tier, ingredient);
        module(tier, module);
        return this;
    }

    public EquipmentFoundryRecipeBuilder ingredient(int tier, Ingredient ingredient) {
        if (ingredients.length >= tier) ingredients = Arrays.copyOf(ingredients, tier+1);
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        ingredients[tier] = ingredient;
        return this;
    }

    public EquipmentFoundryRecipeBuilder module(int tier, ItemModuleType<?> moduleType) {
        if (modules.length >= tier) modules = Arrays.copyOf(modules, tier+1);
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

    public EquipmentFoundryRecipeBuilder module(ItemModuleType<?> moduleType) {
        return module(0, moduleType);
    }

    protected ResourceLocation defaultId() {
        return Objects.requireNonNull(modules[0]).id();
    }

    public void toJson(JsonObject json) {
        json.add("equipment", equipment.toJson());
        JsonArray jsonIngArr = new JsonArray();

        for (var ingredient: ingredients) {
            jsonIngArr.add( ingredient.toJson());
        }

        json.add("ingredients", jsonIngArr);

        JsonArray jsonModuleArr = new JsonArray();
        for (var module: modules) {
            jsonModuleArr.add(module.id().toString());
        }
        json.add("modules", jsonModuleArr);
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FinishedRecipe() {

            @Override
            public void serializeRecipeData(@NotNull JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public @NotNull ResourceLocation getId() {
                var _id = id == null ? defaultId() : id;
                return _id.withPrefix("equipment_foundry/");
            }

            @Override
            public @NotNull RecipeSerializer<?> getType() {
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
