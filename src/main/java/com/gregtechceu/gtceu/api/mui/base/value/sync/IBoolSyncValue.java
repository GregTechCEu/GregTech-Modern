package com.gregtechceu.gtceu.api.mui.base.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.IBoolValue;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface IBoolSyncValue<T> extends IValueSyncHandler<T>, IBoolValue<T>, IIntSyncValue<T> {

    @Override
    default void setBoolValue(boolean val) {
        setBoolValue(val, true, true);
    }

    default void setBoolValue(boolean val, boolean setSource) {
        setBoolValue(val, setSource, true);
    }

    void setBoolValue(boolean value, boolean setSource, boolean sync);

    @Override
    default void setIntValue(int value, boolean setSource, boolean sync) {
        setBoolValue(value == 1, setSource, sync);
    }

    @Override
    default int getIntValue() {
        return IBoolValue.super.getIntValue();
    }

    @Override
    default void setIntValue(int val) {
        IBoolValue.super.setIntValue(val);
    }
}
