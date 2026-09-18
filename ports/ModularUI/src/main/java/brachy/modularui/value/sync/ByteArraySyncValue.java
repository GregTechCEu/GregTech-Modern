package brachy.modularui.value.sync;

import brachy.modularui.utils.serialization.network.ByteBufAdapters;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ByteArraySyncValue extends GenericSyncValue<ByteBuf, byte[]> {

    public ByteArraySyncValue(@NotNull Supplier<byte[]> getter, @Nullable Consumer<byte[]> setter) {
        this(getter, setter, false);
    }

    public ByteArraySyncValue(@NotNull Supplier<byte[]> getter, @Nullable Consumer<byte[]> setter, boolean nullable) {
        super(byte[].class, getter, setter, ByteBufAdapters.BYTE_ARR, byte[]::clone, nullable);
    }
}
