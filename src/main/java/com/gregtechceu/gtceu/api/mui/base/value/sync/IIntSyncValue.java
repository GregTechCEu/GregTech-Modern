package com.gregtechceu.gtceu.api.mui.base.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.IIntValue;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface IIntSyncValue<T> extends IValueSyncHandler<T>, IIntValue<T> {

    @Override
    default void setIntValue(int val) {
        setIntValue(val, true, true);
    }

    default void setIntValue(int val, boolean setSource) {
        setIntValue(val, setSource, true);
    }

    void setIntValue(int value, boolean setSource, boolean sync);
}
