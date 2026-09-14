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

import java.util.Objects;
import java.util.function.Consumer;

@Accessors(chain = true, fluent = true)
public class EquipmentFoundryRecipeBuilder {

    @Setter
    private @Nullable ResourceLocation id;
    @Setter
    private Ingredient equipment;
    @Getter
    private final @Nullable Ingredient[] ingredients = new Ingredient[GTValues.TIER_COUNT];
    @Getter
    private final @Nullable ItemModuleType<?>[] modules = new ItemModuleType[GTValues.TIER_COUNT];

    public EquipmentFoundryRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public EquipmentFoundryRecipeBuilder tier(int tier, Ingredient ingredient, ItemModuleType<?> module) {
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        ingredients[tier] = ingredient;
        modules[tier] = module;
        return this;
    }

    public EquipmentFoundryRecipeBuilder ingredient(int tier, Ingredient ingredient) {
        ingredients[tier] = ingredient;
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

    public EquipmentFoundryRecipeBuilder module(int tier, ItemModuleType<?> moduleType) {
        Preconditions.checkArgument(tier >= 0 && tier <= GTValues.TIER_COUNT, "Invalid tier: %s", tier);
        modules[tier] = moduleType;
        return this;
    }

    public EquipmentFoundryRecipeBuilder module(ItemModuleType<?> moduleType) {
        return module(0, moduleType);
    }

    protected ResourceLocation defaultId() {
        return Objects.requireNonNull(modules[0]).id();
    }

    public void toJson(JsonObject json) {
        json.add("equipment", equipment.toJson());
        JsonArray ingredientArr = new JsonArray();
        for (var ingredient: ingredients) {
            ingredientArr.add(ingredient == null ? JsonNull.INSTANCE : ingredient.toJson());
        }

        json.add("ingredients", ingredientArr);

        JsonArray moduleArr = new JsonArray();
        for (var module: modules) {
            if (module == null) moduleArr.add(JsonNull.INSTANCE);
            else moduleArr.add(module.id().toString());
        }
        json.add("modules", moduleArr);
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
