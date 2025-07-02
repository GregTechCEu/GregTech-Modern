package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;

import java.math.BigInteger;

public class SerializerBigInteger implements IContentSerializer<BigInteger> {

    public static final Codec<BigInteger> CODEC = Codec.STRING.comapFlatMap(str -> {
        try {
            return DataResult.success(new BigInteger(str), Lifecycle.stable());
        } catch (Exception e) {
            return DataResult.error(e::getMessage, Lifecycle.stable());
        }
    }, BigInteger::toString);

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
        if (o instanceof BigInteger b) {
            return b;
        } else if (o instanceof Number n) {
            return BigInteger.valueOf(n.longValue());
        } else if (o instanceof CharSequence) {
            return new BigInteger(o.toString());
        } else {
            return BigInteger.ZERO;
        }
    }

    @Override
    public BigInteger defaultValue() {
        return BigInteger.ZERO;
    }

    @Override
    public Class<BigInteger> contentClass() {
        return BigInteger.class;
    }

    @Override
    public Codec<BigInteger> codec() {
        return CODEC;
    }
}
