package brachy.modularui.api.value;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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
    @UnknownNullability
    T getValue();

    /**
     * Updates the current value.
     *
     * @param value new value
     */
    void setValue(@UnknownNullability T value);

    default T getOrDefault(T defaultValue) {
        T t = getValue();
        return t != null ? t : defaultValue;
    }

    Class<T> getValueType();

    @Override
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
