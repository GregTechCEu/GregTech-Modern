package brachy.modularui.api.value.sync;

import brachy.modularui.api.value.IFloatValue;

import io.netty.buffer.ByteBuf;

public interface IFloatSyncValue<B extends ByteBuf, T> extends IValueSyncHandler<B, T>, IFloatValue<T> {

    @Override
    default void setFloatValue(float val) {
        setFloatValue(val, true, true);
    }

    default void setFloatValue(float val, boolean setSource) {
        setFloatValue(val, setSource, true);
    }

    void setFloatValue(float value, boolean setSource, boolean sync);
}
