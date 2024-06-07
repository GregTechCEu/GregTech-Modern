package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;

import java.math.BigInteger;

/**
 * @author KilaBash
 * @date 2022/06/22
 * @implNote SerializerBigInteger
 */
public class SerializerBigInteger implements IContentSerializer<BigInteger> {

    public static final Codec<BigInteger> CODEC = Codec.STRING.comapFlatMap(str -> {
        try {
            return DataResult.success(new BigInteger(str), Lifecycle.stable());
        } catch (Exception e) {
            return DataResult.error(e::getMessage, Lifecycle.stable());
        }
    },
            BigInteger::toString);

    public static SerializerBigInteger INSTANCE = new SerializerBigInteger();

    private SerializerBigInteger() {}

    @Override
    public Codec<BigInteger> codec() {
        return CODEC;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, BigInteger content) {
        buf.writeUtf(content.toString());
    }

    @Override
    public BigInteger fromNetwork(RegistryFriendlyByteBuf buf) {
        return new BigInteger(buf.readUtf());
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
