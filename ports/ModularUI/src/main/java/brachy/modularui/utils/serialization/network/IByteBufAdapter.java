package brachy.modularui.utils.serialization.network;

import brachy.modularui.utils.EqualityTest;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface IByteBufAdapter<B, V> extends StreamCodec<B, V>, EqualityTest<V> {

    @Override
    @NotNull
    V decode(@NotNull B buffer);

    @Override
    void encode(@NotNull B buffer, @NotNull V u);

    @Override
    boolean areEqual(@NotNull V v1, @NotNull V v2);

    @SuppressWarnings("ConstantValue")
    static <B extends ByteBuf, V> StreamEncoder<B, V> wrapNullSafe(StreamEncoder<B, V> serializer) {
        return (buffer, value) -> {
            buffer.writeBoolean(value == null);
            if (value != null) {
                serializer.encode(buffer, value);
            }
        };
    }

    @SuppressWarnings("DataFlowIssue")
    static <B extends ByteBuf, V> StreamDecoder<B, V> wrapNullSafe(StreamDecoder<B, V> deserializer) {
        return buffer -> buffer.readBoolean() ? null : deserializer.decode(buffer);
    }
}
