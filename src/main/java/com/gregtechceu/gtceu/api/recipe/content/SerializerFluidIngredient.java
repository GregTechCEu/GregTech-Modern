package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.ingredient.OldFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.FLUID_SERIALIZERS;

public class SerializerFluidIngredient implements IContentSerializer<OldFluidIngredient> {

    public static SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    static {
        FLUID_SERIALIZERS.unfreeze();
        FLUID_SERIALIZERS.register("OldFluidIngredient", OldFluidIngredient::fromNetwork);
        FLUID_SERIALIZERS.register("IntProviderFluidIngredient", IntProviderFluidIngredient::fromNetwork);
    };

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, OldFluidIngredient content) {
        String name = content.getClass().getSimpleName();
        if (!FLUID_SERIALIZERS.containKey(name)) {
            throw new IllegalArgumentException(
                    "SerializerFluidIngredient tried to serialize a OldFluidIngredient's subclass %s, which is not in the FluidSerializers registry!"
                            .formatted(name));
        }
        buf.writeUtf(name);
        content.toNetwork(buf);
    }

    @Override
    public OldFluidIngredient fromNetwork(FriendlyByteBuf buf) {
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
    public OldFluidIngredient fromJson(JsonElement json) {
        return OldFluidIngredient.fromJson(json);
    }

    @Override
    public JsonElement toJson(OldFluidIngredient content) {
        return content.toJson();
    }

    @Override
    public OldFluidIngredient of(Object o) {
        if (o instanceof OldFluidIngredient ingredient) {
            return ingredient.copy();
        }
        if (o instanceof FluidStack stack) {
            return OldFluidIngredient.of(stack.copy());
        }
        return OldFluidIngredient.EMPTY;
    }

    @Override
    public OldFluidIngredient defaultValue() {
        return OldFluidIngredient.EMPTY;
    }

    @Override
    public Class<OldFluidIngredient> contentClass() {
        return OldFluidIngredient.class;
    }

    @Override
    public Codec<OldFluidIngredient> codec() {
        return OldFluidIngredient.CODEC;
    }
}
