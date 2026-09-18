package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.IIntValue;

import io.netty.buffer.ByteBuf;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface IIntSyncValue<B extends ByteBuf, T> extends IValueSyncHandler<B, T>, IIntValue<T> {

    @Override
    default void setIntValue(int val) {
        setIntValue(val, true, true);
    }

    default void setIntValue(int val, boolean setSource) {
        setIntValue(val, setSource, true);
    }

    void setIntValue(int value, boolean setSource, boolean sync);
}
