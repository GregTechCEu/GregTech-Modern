package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.IByteValue;

import io.netty.buffer.ByteBuf;

public interface IByteSyncValue<B extends ByteBuf, T> extends IByteValue<T>, IValueSyncHandler<B, T> {

    @Override
    default void setByteValue(byte val) {
        setByteValue(val, true);
    }

    default void setByteValue(byte val, boolean setSource) {
        setByteValue(val, setSource, true);
    }

    void setByteValue(byte value, boolean setSource, boolean sync);
}
