package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import net.minecraft.network.FriendlyByteBuf;

import com.mojang.serialization.Codec;
import org.apache.commons.lang3.math.NumberUtils;

public class SerializerEnergyStack implements IContentSerializer<EnergyStack> {

    public static SerializerEnergyStack INSTANCE = new SerializerEnergyStack();

    private SerializerEnergyStack() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, EnergyStack content) {
        content.toNetwork(buf);
    }

    @Override
    public EnergyStack fromNetwork(FriendlyByteBuf buf) {
        return EnergyStack.fromNetwork(buf);
    }

    @Override
    public EnergyStack of(Object o) {
        if (o instanceof EnergyStack stack) {
            return stack;
        } else if (o instanceof Number n) {
            return new EnergyStack(n.longValue());
        } else if (o instanceof CharSequence) {
            return new EnergyStack(NumberUtils.toLong(o.toString()));
        } else {
            return EnergyStack.EMPTY;
        }
    }

    @Override
    public EnergyStack defaultValue() {
        return EnergyStack.EMPTY;
    }

    @Override
    public Class<EnergyStack> contentClass() {
        return EnergyStack.class;
    }

    @Override
    public Codec<EnergyStack> codec() {
        return EnergyStack.CODEC;
    }
}
