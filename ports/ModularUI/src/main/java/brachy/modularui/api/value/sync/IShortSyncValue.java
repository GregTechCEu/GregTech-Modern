package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.IShortValue;

import io.netty.buffer.ByteBuf;

/**
 * A helper interface for sync values which can be turned into a short.
 *
 * @param <T> value type
 */
public interface IShortSyncValue<B extends ByteBuf, T> extends IValueSyncHandler<B, T>, IShortValue<T> {

    @Override
    default void setShortValue(short val) {
        setShortValue(val, true, true);
    }

    default void setShortValue(short val, boolean setSource) {
        setShortValue(val, setSource, true);
    }

    void setShortValue(short value, boolean setSource, boolean sync);
}
