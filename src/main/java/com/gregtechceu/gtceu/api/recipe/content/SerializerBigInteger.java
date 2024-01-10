package com.gregtechceu.gtceu.api.recipe.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;

import java.math.BigInteger;

/**
 * @author KilaBash
 * @date 2022/06/22
 * @implNote SerializerBigInteger
 */
public class SerializerBigInteger implements IContentSerializer<BigInteger>{

    public static SerializerBigInteger INSTANCE = new SerializerBigInteger();

    private SerializerBigInteger() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, BigInteger content) {
        buf.writeUtf(content.toString());
    }

    @Override
    public BigInteger fromNetwork(FriendlyByteBuf buf) {
        return new BigInteger(buf.readUtf());
    }

    @Override
    public BigInteger fromJson(JsonElement json) {
        return json.getAsBigInteger();
    }

    @Override
    public JsonElement toJson(BigInteger content) {
        return new JsonPrimitive(content);
    }

    @Override
    public BigInteger of(Object o) {
        if (o instanceof BigInteger) {
            return (BigInteger) o;
        } else if (o instanceof Number) {
            return BigInteger.valueOf(((Number) o).longValue());
        } else if (o instanceof CharSequence) {
            return new BigInteger(o.toString());
        }
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger defaultValue() {
        return BigInteger.ZERO;
    }

}
