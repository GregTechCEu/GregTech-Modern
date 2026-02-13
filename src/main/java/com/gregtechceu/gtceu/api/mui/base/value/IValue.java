package com.gregtechceu.gtceu.api.mui.base.value;

/**
 * A value wrapper for widgets.
 *
 * @param <T> value type
 */
public interface IValue<T> extends ISyncOrValue {

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

    Class<T> getValueType();

    default boolean isValueOfType(Class<?> type) {
        return type.isAssignableFrom(getValueType());
    }

    @SuppressWarnings("unchecked")
    @Override
    default <V> IValue<V> castValueNullable(Class<V> valueType) {
        return isValueOfType(valueType) ? (IValue<V>) this : null;
    }

    @Override
    default boolean isValueHandler() {
        return true;
    }
}
