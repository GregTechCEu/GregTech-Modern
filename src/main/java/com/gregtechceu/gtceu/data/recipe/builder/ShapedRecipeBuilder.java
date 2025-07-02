package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.StrictShapedRecipe;

import com.lowdragmc.lowdraglib.utils.Builder;
import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;

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
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ShapedRecipeBuilder extends Builder<Ingredient, ShapedRecipeBuilder> {

    protected ItemStack output = ItemStack.EMPTY;
    protected ResourceLocation id;
    protected String group;
    protected boolean isStrict;

    public ShapedRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public ShapedRecipeBuilder() {
        this(null);
    }

    public ShapedRecipeBuilder pattern(String slice) {
        return aisle(slice);
    }

    public ShapedRecipeBuilder define(char cha, TagKey<Item> itemStack) {
        return where(cha, Ingredient.of(itemStack));
    }

    public ShapedRecipeBuilder define(char cha, ItemStack itemStack) {
        return where(cha, itemStack.hasTag() ? StrictNBTIngredient.of(itemStack) : Ingredient.of(itemStack));
    }

    public ShapedRecipeBuilder define(char cha, ItemLike itemLike) {
        return where(cha, Ingredient.of(itemLike));
    }

    public ShapedRecipeBuilder define(char cha, Ingredient ingredient) {
        return where(cha, ingredient);
    }

    public ShapedRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return (ShapedRecipeBuilder) this;
    }

    public ShapedRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return (ShapedRecipeBuilder) this;
    }

    public ShapedRecipeBuilder output(ItemStack itemStack, int count, CompoundTag nbt) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        this.output.setTag(nbt);
        return this;
    }

    public ShapedRecipeBuilder id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public ShapedRecipeBuilder id(String id) {
        this.id = new ResourceLocation(id);
        return this;
    }

    public ShapedRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ShapedRecipeBuilder isStrict(boolean isStrict) {
        this.isStrict = isStrict;
        return this;
    }

    @Override
    public ShapedRecipeBuilder shallowCopy() {
        var builder = super.shallowCopy();
        builder.output = output.copy();
        return builder;
    }

    public void toJson(JsonObject json) {
        if (group != null) {
            json.addProperty("group", group);
        }

        if (!shape.isEmpty()) {
            JsonArray pattern = new JsonArray();
            for (String[] strings : shape) {
                for (String string : strings) {
                    pattern.add(string);
                }
            }
            json.add("pattern", pattern);
        }

        if (!symbolMap.isEmpty()) {
            JsonObject key = new JsonObject();
            symbolMap.forEach((k, v) -> key.add(k.toString(), v.toJson()));
            json.add("key", key);
        }

        if (output.isEmpty()) {
            GTCEu.LOGGER.error("shaped recipe {} output is empty", id);
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
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
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
                return new ResourceLocation(ID.getNamespace(), "shaped" + "/" + ID.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return isStrict ? StrictShapedRecipe.SERIALIZER : RecipeSerializer.SHAPED_RECIPE;
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
