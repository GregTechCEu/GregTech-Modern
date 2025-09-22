package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.math.NumberUtils;

public class SerializerLong implements IContentSerializer<Long> {

    public static SerializerLong INSTANCE = new SerializerLong();

    private SerializerLong() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, Long content) {
        buf.writeVarLong(content);
    }

    @Override
    public Long fromNetwork(FriendlyByteBuf buf) {
        return buf.readVarLong();
    }

    @Override
    public Long fromJson(JsonElement json) {
        return json.getAsLong();
    }

    @Override
    public JsonElement toJson(Long content) {
        return new JsonPrimitive(content);
    }

    @Override
    public Long of(Object o) {
        if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof Number) {
            return ((Number) o).longValue();
        } else if (o instanceof CharSequence) {
            return NumberUtils.toLong(o.toString(), 1);
        }
        return 0L;
    }

    @Override
    public Long defaultValue() {
        return 0L;
    }

    @Override
    public Class<Long> contentClass() {
        return Long.class;
    }

    @Override
    public Codec<Long> codec() {
        return Codec.LONG;
    }
}
