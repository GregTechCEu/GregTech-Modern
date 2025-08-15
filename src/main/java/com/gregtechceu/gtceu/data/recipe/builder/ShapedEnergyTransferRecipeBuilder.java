package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;

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

public class ShapedEnergyTransferRecipeBuilder extends Builder<Ingredient, ShapedEnergyTransferRecipeBuilder> {

    protected ItemStack output = ItemStack.EMPTY;
    protected Ingredient chargeIngredient = Ingredient.EMPTY;
    protected ResourceLocation id;
    protected String group;
    protected boolean transferMaxCharge;
    protected boolean overrideCharge;

    public ShapedEnergyTransferRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public ShapedEnergyTransferRecipeBuilder() {
        this(null);
    }

    public ShapedEnergyTransferRecipeBuilder pattern(String slice) {
        return aisle(slice);
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, TagKey<Item> itemStack) {
        return where(cha, Ingredient.of(itemStack));
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, ItemStack itemStack) {
        return where(cha, itemStack.hasTag() ? StrictNBTIngredient.of(itemStack) : Ingredient.of(itemStack));
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, ItemLike itemLike) {
        return where(cha, Ingredient.of(itemLike));
    }

    public ShapedEnergyTransferRecipeBuilder define(char cha, Ingredient ingredient) {
        return where(cha, ingredient);
    }

    public ShapedEnergyTransferRecipeBuilder chargeIngredient(Ingredient chargeIngredient) {
        this.chargeIngredient = chargeIngredient;
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder overrideCharge(boolean overrideCharge) {
        this.overrideCharge = overrideCharge;
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder transferMaxCharge(boolean transferMaxCharge) {
        this.transferMaxCharge = transferMaxCharge;
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder output(ItemStack itemStack, int count, CompoundTag nbt) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        this.output.setTag(nbt);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder id(String id) {
        this.id = new ResourceLocation(id);
        return this;
    }

    public ShapedEnergyTransferRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public ShapedEnergyTransferRecipeBuilder shallowCopy() {
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

        json.addProperty("overrideCharge", overrideCharge);
        json.addProperty("transferMaxCharge", transferMaxCharge);
        if (chargeIngredient.isEmpty()) {
            GTCEu.LOGGER.error("shaped energy transfer recipe {} chargeIngredient is empty", id);
            throw new IllegalArgumentException(id + ": chargeIngredient is empty");
        } else {
            json.add("chargeIngredient", chargeIngredient.toJson());
        }
        if (output.isEmpty()) {
            GTCEu.LOGGER.error("shaped energy transfer recipe {} output is empty", id);
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
                return ShapedEnergyTransferRecipe.SERIALIZER;
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
