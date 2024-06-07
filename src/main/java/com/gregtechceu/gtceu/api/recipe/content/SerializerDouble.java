package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;
import org.apache.commons.lang3.math.NumberUtils;

public class SerializerDouble implements IContentSerializer<Double> {

    public static SerializerDouble INSTANCE = new SerializerDouble();

    private SerializerDouble() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Double content) {
        buf.writeDouble(content);
    }

    @Override
    public Double fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readDouble();
    }

    @Override
    public Codec<Double> codec() {
        return Codec.DOUBLE;
    }

    @Override
    public Double of(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o instanceof CharSequence) {
            return NumberUtils.toDouble(o.toString(), 1);
        }
        return 0d;
    }

    @Override
    public Double defaultValue() {
        return 0d;
    }
}
