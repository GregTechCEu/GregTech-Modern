package com.gregtechceu.gtceu.api.mui.base.value;

/**
 * A value wrapper for widgets.
 *
 * @param <T> value type
 */
public interface IValue<T> {

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    T getValue();

    /**
     * Updates the current value.
     *
     * @param value new value
     */
    void setValue(T value);
}
