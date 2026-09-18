package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.ILongValue;

import io.netty.buffer.ByteBuf;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface ILongSyncValue<B extends ByteBuf, T> extends IValueSyncHandler<B, T>, ILongValue<T> {

    @Override
    default void setLongValue(long val) {
        setLongValue(val, true, true);
    }

    default void setLongValue(long val, boolean setSource) {
        setLongValue(val, setSource, true);
    }

    void setLongValue(long value, boolean setSource, boolean sync);
}
