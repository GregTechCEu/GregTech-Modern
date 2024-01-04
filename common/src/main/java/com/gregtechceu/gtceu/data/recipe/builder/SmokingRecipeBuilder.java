package com.gregtechceu.gtceu.data.recipe.builder;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.ingredient.NBTIngredient;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote SmeltingRecipeBuilder
 */
@Accessors(chain = true, fluent = true)
public class SmokingRecipeBuilder {
    private Ingredient input;
    @Setter
    protected String group;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    private float experience;
    @Setter
    private int cookingTime;
    @Setter
    protected ResourceLocation id;

    public SmokingRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public SmokingRecipeBuilder input(TagKey<Item> itemStack) {
        return input(Ingredient.of(itemStack));
    }

    public SmokingRecipeBuilder input(ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.getDamageValue() >0) {
            input = NBTIngredient.createNBTIngredient(itemStack);
        }else {
            input = Ingredient.of(itemStack);
        }
        return this;
    }

    public SmokingRecipeBuilder input(ItemLike itemLike) {
        return input(Ingredient.of(itemLike));
    }

    public SmokingRecipeBuilder input(Ingredient ingredient) {
        input = ingredient;
        return this;
    }

    public SmokingRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public SmokingRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    public SmokingRecipeBuilder output(ItemStack itemStack, int count, CompoundTag nbt) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        this.output.setTag(nbt);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    public void toJson(JsonObject json) {
        if (group != null) {
            json.addProperty("group", group);
        }

        if (!input.isEmpty()) {
            json.add("ingredient", input.toJson());
        }

        if (output.isEmpty()) {
            LDLib.LOGGER.error("shapeless recipe {} output is empty", id);
            throw new IllegalArgumentException(id + ": output items is empty");
        } else {
            JsonObject result = new JsonObject();
            result.addProperty("item", BuiltInRegistries.ITEM.getKey(output.getItem()).toString());
            if (output.getCount() > 1) {
                result.addProperty("count", output.getCount());
            }
            if (output.hasTag() && output.getTag() != null) {
                result.add("nbt", NBTToJsonConverter.getObject(output.getTag()));
            }
            json.add("result", result);
        }

        json.addProperty("experience", experience);
        json.addProperty("cookingtime", cookingTime);
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public ResourceLocation getId() {
                var ID = id == null ? defaultId() : id;
                return new ResourceLocation(ID.getNamespace(), "smoking" + "/" + ID.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return RecipeSerializer.BLASTING_RECIPE;
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
