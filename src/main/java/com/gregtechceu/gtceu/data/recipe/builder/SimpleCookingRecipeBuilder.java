package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Accessors(chain = true, fluent = true)
@SuppressWarnings("deprecation")
public class SimpleCookingRecipeBuilder<T extends AbstractCookingRecipe> {

    protected final String folder;
    protected final RecipeSerializer<T> serializer;
    protected @Nullable Ingredient input;
    @Setter
    protected @Nullable String group;
    @Setter
    protected CookingBookCategory category = CookingBookCategory.MISC;

    protected ItemStack output = ItemStack.EMPTY;
    @Setter
    protected float experience;
    @Setter
    protected int cookingTime;
    @Setter
    protected @Nullable ResourceLocation id;

    protected SimpleCookingRecipeBuilder(@Nullable ResourceLocation id, String folder, RecipeSerializer<T> serializer) {
        this.id = id;
        this.folder = folder;
        this.serializer = serializer;
    }

    public static SimpleCookingRecipeBuilder<CampfireCookingRecipe> campfireCooking(@Nullable ResourceLocation id) {
        return new SimpleCookingRecipeBuilder<>(id, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder<SmeltingRecipe> smelting(@Nullable ResourceLocation id) {
        return new SimpleCookingRecipeBuilder<>(id, "smelting", RecipeSerializer.SMELTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder<BlastingRecipe> blasting(@Nullable ResourceLocation id) {
        return new SimpleCookingRecipeBuilder<>(id, "blasting", RecipeSerializer.BLASTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder<SmokingRecipe> smoking(@Nullable ResourceLocation id) {
        return new SimpleCookingRecipeBuilder<>(id, "smoking", RecipeSerializer.SMOKING_RECIPE);
    }

    public SimpleCookingRecipeBuilder<T> input(TagKey<Item> tag) {
        return input(Ingredient.of(tag));
    }

    public SimpleCookingRecipeBuilder<T> input(ItemStack itemStack) {
        input = itemStack.hasTag() ? StrictNBTIngredient.of(itemStack) : Ingredient.of(itemStack);
        return this;
    }

    public SimpleCookingRecipeBuilder<T> input(ItemLike itemLike) {
        return input(Ingredient.of(itemLike));
    }

    public SimpleCookingRecipeBuilder<T> input(Ingredient ingredient) {
        input = ingredient;
        return this;
    }

    public SimpleCookingRecipeBuilder<T> output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public SimpleCookingRecipeBuilder<T> output(ItemStack itemStack, int count) {
        this.output = itemStack.copyWithCount(count);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void toJson(JsonObject json) {
        if (group != null) {
            json.addProperty("group", group);
        }

        if (input == null || input.isEmpty()) {
            GTCEu.LOGGER.error("{} recipe {} input is empty", folder, id);
            throw new IllegalArgumentException(id + ": input item is empty");
        }
        if (output.isEmpty()) {
            GTCEu.LOGGER.error("{} recipe {} output is empty", folder, id);
            throw new IllegalArgumentException(id + ": output item is empty");
        }

        json.add("ingredient", input.toJson());

        JsonObject result = new JsonObject();
        result.addProperty("item", BuiltInRegistries.ITEM.getKey(output.getItem()).toString());
        if (output.getCount() > 1) {
            result.addProperty("count", output.getCount());
        }
        if (output.hasTag() && output.getTag() != null) {
            result.add("nbt", NBTToJsonConverter.getObject(output.getTag()));
        }
        json.add("result", result);

        json.addProperty("experience", experience);
        json.addProperty("cookingtime", cookingTime);
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        ResourceLocation recipeId = (id == null ? defaultId() : id).withPrefix(folder + "/");

        consumer.accept(new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return serializer;
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
