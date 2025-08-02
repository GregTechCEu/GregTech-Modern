package com.gregtechceu.gtceu.api.mui.base.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.IStringValue;

/**
 * A helper interface for sync values which can be turned into a string.
 *
 * @param <T> value type
 */
public interface IStringSyncValue<T> extends IValueSyncHandler<T>, IStringValue<T> {

    @Override
    default void setStringValue(String val) {
        setStringValue(val, true, true);
    }

    default void setStringValue(String val, boolean setSource) {
        setStringValue(val, setSource, true);
    }

    void setStringValue(String value, boolean setSource, boolean sync);
}
