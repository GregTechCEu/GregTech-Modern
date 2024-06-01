package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedSingleFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedTagFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

public class SerializerFluidIngredient implements IContentSerializer<FluidIngredient> {

    public static SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, FluidIngredient content) {
        FluidIngredient.STREAM_CODEC.encode(buf, content);
    }

    @Override
    public FluidIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return FluidIngredient.STREAM_CODEC.decode(buf);
    }

    @Override
    public FluidIngredient fromJson(JsonElement json, HolderLookup.Provider provider) {
        return FluidIngredient.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
    }

    @Override
    public JsonElement toJson(FluidIngredient content, HolderLookup.Provider provider) {
        return FluidIngredient.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), content)
                .getOrThrow();
    }

    @Override
    public FluidIngredient of(Object o) {
        if (o instanceof SizedTagFluidIngredient ingredient) {
            return ingredient.copy();
        } else if (o instanceof SizedSingleFluidIngredient ingredient) {
            return ingredient.copy();
        }
        if (o instanceof FluidStack stack) {
            return new SizedSingleFluidIngredient(stack.getFluidHolder(), stack.getAmount());
        }
        return FluidIngredient.empty();
    }

    @Override
    public FluidIngredient defaultValue() {
        return FluidIngredient.empty();
    }
}
