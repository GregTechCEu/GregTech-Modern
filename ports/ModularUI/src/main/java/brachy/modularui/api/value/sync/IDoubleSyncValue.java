package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.IDoubleValue;

import io.netty.buffer.ByteBuf;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface IDoubleSyncValue<B extends ByteBuf, T> extends IValueSyncHandler<B, T>, IDoubleValue<T> {

    @Override
    default void setDoubleValue(double val) {
        setDoubleValue(val, true, true);
    }

    default void setDoubleValue(double val, boolean setSource) {
        setDoubleValue(val, setSource, true);
    }

    void setDoubleValue(double value, boolean setSource, boolean sync);
}
