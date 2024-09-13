package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonElement;

public class SerializerIngredient implements IContentSerializer<Ingredient> {

    public static final Codec<Ingredient> CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> Ingredient.fromJson(dynamic.convert(JsonOps.INSTANCE).getValue()),
            ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));

    public static SerializerIngredient INSTANCE = new SerializerIngredient();

    private SerializerIngredient() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, Ingredient content) {
        content.toNetwork(buf);
    }

    @Override
    public Ingredient fromNetwork(FriendlyByteBuf buf) {
        return Ingredient.fromNetwork(buf);
    }

    @Override
    public Ingredient fromJson(JsonElement json) {
        return Ingredient.fromJson(json);
    }

    @Override
    public JsonElement toJson(Ingredient content) {
        return content.toJson();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Ingredient of(Object o) {
        if (o instanceof Ingredient ingredient) {
            return ingredient;
        } else if (o instanceof ItemStack itemStack) {
            return SizedIngredient.create(itemStack);
        } else if (o instanceof ItemLike itemLike) {
            return Ingredient.of(itemLike);
        } else if (o instanceof TagKey tag) {
            return Ingredient.of(tag);
        }
        return Ingredient.EMPTY;
    }

    @Override
    public Ingredient defaultValue() {
        return Ingredient.EMPTY;
    }

    @Override
    public Codec<Ingredient> codec() {
        return CODEC;
    }
}
