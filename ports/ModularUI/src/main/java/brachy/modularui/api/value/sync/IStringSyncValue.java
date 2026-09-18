package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.IStringValue;

import io.netty.buffer.ByteBuf;

/**
 * A helper interface for sync values which can be turned into a string.
 *
 * @param <T> value type
 */
public interface IStringSyncValue<B extends ByteBuf, T> extends IValueSyncHandler<B, T>, IStringValue<T> {

    @Override
    default void setStringValue(String val) {
        setStringValue(val, true, true);
    }

    default void setStringValue(String val, boolean setSource) {
        setStringValue(val, setSource, true);
    }

    void setStringValue(String value, boolean setSource, boolean sync);
}
