package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.FLUID_SERIALIZERS;

public class SerializerFluidIngredient implements IContentSerializer<FluidIngredient> {

    public static SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    static {
        FLUID_SERIALIZERS.unfreeze();
        FLUID_SERIALIZERS.register("FluidIngredient", FluidIngredient::fromNetwork);
        FLUID_SERIALIZERS.register("IntProviderFluidIngredient", IntProviderFluidIngredient::fromNetwork);
    };

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, FluidIngredient content) {
        String name = content.getClass().getSimpleName();
        if (!FLUID_SERIALIZERS.containKey(name)) {
            throw new IllegalArgumentException(
                    "SerializerFluidIngredient tried to serialize a FluidIngredient's subclass %s, which is not in the FluidSerializers registry!"
                            .formatted(name));
        }
        buf.writeUtf(name);
        content.toNetwork(buf);
    }

    @Override
    public FluidIngredient fromNetwork(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        var fromNetworkFunction = FLUID_SERIALIZERS.get(name);
        if (fromNetworkFunction == null) {
            throw new IllegalArgumentException(
                    "SerializerFluidIngredient's fromNetwork received a package containing an invalid FluidSerializer name." +
                            " Name: %s ".formatted(name));
        }
        return fromNetworkFunction.apply(buf);
    }

    @Override
    public FluidIngredient fromJson(JsonElement json) {
        return FluidIngredient.fromJson(json);
    }

    @Override
    public JsonElement toJson(FluidIngredient content) {
        return content.toJson();
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

    @Override
    public Class<FluidIngredient> contentClass() {
        return FluidIngredient.class;
    }

    @Override
    public Codec<FluidIngredient> codec() {
        return FluidIngredient.CODEC;
    }
}
