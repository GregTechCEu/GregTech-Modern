package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.utils.serialization.network.IByteBufAdapter;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufDeserializer;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufSerializer;

import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;

public interface ICopy<T> {

    static <T> ICopy<T> immutable() {
        return t -> t;
    }

    static <T> ICopy<T> ofSerializer(IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer) {
        return t -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            serializer.serialize(buf, t);
            return deserializer.deserialize(buf);
        };
    }

    static <T> ICopy<T> ofSerializer(IByteBufAdapter<T> adapter) {
        return ofSerializer(adapter, adapter);
    }

    T createDeepCopy(T t);
}
