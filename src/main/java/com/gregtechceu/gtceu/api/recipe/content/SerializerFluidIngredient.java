package com.gregtechceu.gtceu.api.recipe.content;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;

public class SerializerFluidIngredient implements IContentSerializer<FluidIngredient> {

    public static SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, FluidIngredient content) {
        content.toNetwork(buf);
    }

    @Override
    public FluidIngredient fromNetwork(FriendlyByteBuf buf) {
        return FluidIngredient.fromNetwork(buf);
    }

    @Override
    public FluidIngredient fromJson(JsonElement json) {
        return FluidIngredient.fromJson(json);
    }

    @Override
    public JsonElement toJson(FluidIngredient content) {
        return FluidIngredient.CODEC.encodeStart(JsonOps.INSTANCE, content).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public FluidIngredient of(Object o) {
        if (o instanceof FluidIngredient ingredient) {
            return ingredient.copy();
        }
        if (o instanceof FluidStack stack) {
            return FluidIngredient.of(stack.copy());
        }
        return FluidIngredient.EMPTY;
    }

    @Override
    public FluidIngredient defaultValue() {
        return FluidIngredient.EMPTY;
    }
}
