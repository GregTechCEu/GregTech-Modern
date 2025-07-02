package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.math.NumberUtils;

public class SerializerInteger implements IContentSerializer<Integer> {

    public static SerializerInteger INSTANCE = new SerializerInteger();

    private SerializerInteger() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, Integer content) {
        buf.writeInt(content);
    }

    @Override
    public Integer fromNetwork(FriendlyByteBuf buf) {
        return buf.readInt();
    }

    @Override
    public Integer fromJson(JsonElement json) {
        return json.getAsInt();
    }

    @Override
    public JsonElement toJson(Integer content) {
        return new JsonPrimitive(content);
    }

    @Override
    public Integer of(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof CharSequence) {
            return NumberUtils.toInt(o.toString(), 1);
        }
        return 0;
    }

    @Override
    public Integer defaultValue() {
        return 0;
    }

    @Override
    public Class<Integer> contentClass() {
        return Integer.class;
    }

    @Override
    public Codec<Integer> codec() {
        return Codec.INT;
    }
}
