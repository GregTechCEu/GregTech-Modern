package brachy.modularui.utils;

import brachy.modularui.core.extensions.IRegistryFriendlyByteBufExtension;
import brachy.modularui.utils.serialization.network.IByteBufAdapter;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import io.netty.buffer.ByteBuf;

public interface ICopy<T> {

    static <T> ICopy<T> immutable() {
        return t -> t;
    }

    @SuppressWarnings("unchecked")
    static <B extends ByteBuf, T> ICopy<T> ofSerializer(StreamEncoder<B, T> serializer, StreamDecoder<B, T> deserializer) {
        return t -> {
            // the lowest subclass of ByteBuf is RegistryFriendlyByteBuf so this *should* work
            RegistryFriendlyByteBuf buf = IRegistryFriendlyByteBufExtension.createEmpty(RegistryAccessContainer.current());
            serializer.encode((B) buf, t);
            return deserializer.decode((B) buf);
        };
    }

    static <B extends ByteBuf, T> ICopy<T> ofSerializer(IByteBufAdapter<B, T> adapter) {
        return ofSerializer(adapter, adapter);
    }

    T createDeepCopy(T t);

    static <T> ICopy<T> wrapNullSafe(ICopy<T> copy) {
        return t -> t == null ? null : copy.createDeepCopy(t);
    }
}
