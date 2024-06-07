package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;

public class SerializerFluidIngredient implements IContentSerializer<SizedFluidIngredient> {

    public static SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, SizedFluidIngredient content) {
        SizedFluidIngredient.STREAM_CODEC.encode(buf, content);
    }

    @Override
    public SizedFluidIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return SizedFluidIngredient.STREAM_CODEC.decode(buf);
    }

    @Override
    public Codec<SizedFluidIngredient> codec() {
        return SizedFluidIngredient.NESTED_CODEC;
    }

    @Override
    public SizedFluidIngredient of(Object o) {
        if (o instanceof SizedFluidIngredient ingredient) {
            return new SizedFluidIngredient(ingredient.ingredient(), ingredient.amount());
        }
        if (o instanceof FluidStack stack) {
            return new SizedFluidIngredient(FluidIngredient.single(stack.getFluid()), stack.getAmount());
        }
        return defaultValue();
    }

    @Override
    public SizedFluidIngredient defaultValue() {
        return new SizedFluidIngredient(FluidIngredient.empty(), 1);
    }
}
