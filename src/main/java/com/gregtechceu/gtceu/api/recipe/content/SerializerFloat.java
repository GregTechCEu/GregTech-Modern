package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.math.NumberUtils;

public class SerializerFloat implements IContentSerializer<Float> {

    public static SerializerFloat INSTANCE = new SerializerFloat();

    private SerializerFloat() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, Float content) {
        buf.writeFloat(content);
    }

    @Override
    public Float fromNetwork(FriendlyByteBuf buf) {
        return buf.readFloat();
    }

    @Override
    public Float fromJson(JsonElement json) {
        return json.getAsFloat();
    }

    @Override
    public JsonElement toJson(Float content) {
        return new JsonPrimitive(content);
    }

    @Override
    public Float of(Object o) {
        if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof CharSequence) {
            return NumberUtils.toFloat(o.toString(), 1);
        }
        return 0f;
    }

    @Override
    public Float defaultValue() {
        return 0f;
    }

    @Override
    public Class<Float> contentClass() {
        return Float.class;
    }

    @Override
    public Codec<Float> codec() {
        return Codec.FLOAT;
    }
}
