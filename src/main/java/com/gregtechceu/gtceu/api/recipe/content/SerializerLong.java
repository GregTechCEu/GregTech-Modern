package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author KilaBash
 * @date 2022/06/22
 * @implNote SerializerLong
 */
public class SerializerLong implements IContentSerializer<Long> {

    public static SerializerLong INSTANCE = new SerializerLong();

    private SerializerLong() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, Long content) {
        buf.writeVarLong(content);
    }

    @Override
    public Long fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readVarLong();
    }

    @Override
    public Long fromJson(JsonElement json, HolderLookup.Provider provider) {
        return json.getAsLong();
    }

    @Override
    public JsonElement toJson(Long content, HolderLookup.Provider provider) {
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
}
