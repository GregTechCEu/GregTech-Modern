package com.gregtechceu.gtceu.api.mui.base.value;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that is implemented on {@link IValue} and {@link com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler
 * SyncHandler} for easier
 * validation and setters.
 */
@ApiStatus.NonExtendable
public interface ISyncOrValue {

    /**
     * A sync handler or value representing null.
     */
    ISyncOrValue EMPTY = new ISyncOrValue() {

        @Override
        public <T> @Nullable T castNullable(Class<T> type) {
            return null;
        }

        @Override
        public boolean isTypeOrEmpty(Class<?> type) {
            return true;
        }
    };

    /**
     * Returns the given sync handler or value or {@link #EMPTY} if null.
     *
     * @param syncOrValue sync handler or value
     * @return a non-null representation of the given sync handler or value
     */
    @NotNull
    static ISyncOrValue orEmpty(@Nullable ISyncOrValue syncOrValue) {
        return syncOrValue != null ? syncOrValue : EMPTY;
    }

    /**
     * Returns if this sync handler or value is an instance of the given type or if this represents null. This is
     * useful, when the value or
     * sync handler can be null in the widget.
     *
     * @param type type to check for
     * @return if this sync handler or value is an instance of the type or empty
     */
    default boolean isTypeOrEmpty(Class<?> type) {
        return type.isAssignableFrom(getClass());
    }

    /**
     * Casts this sync handler or value to the given type or null if this isn't a subtype of the given type.
     *
     * @param type type to cast this sync handle or value to
     * @param <T>  type to cast to
     * @return this cast sync handler or value
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T castNullable(Class<T> type) {
        return type.isAssignableFrom(getClass()) ? (T) this : null;
    }

    /**
     * Casts this sync handler or value to a {@link IValue IValue&lt;V&gt;} if it is a value handler and the containing
     * value is of type
     * {@link V} else null.
     *
     * @param valueType expected type of the containing value
     * @param <V>       expected type of the containing value
     * @return a {@link IValue IValue&lt;V&gt;} if types match or null
     */
    @Nullable
    default <V> IValue<V> castValueNullable(Class<V> valueType) {
        return null;
    }

    /**
     * Casts this sync handler or value to the given type or throws an exception if this isn't a subtype of the given
     * type.
     *
     * @param type type to cast this sync handle or value to
     * @param <T>  type to cast to
     * @return this cast sync handler or value
     * @throws IllegalStateException if this is not a subtype of the given type
     */
    default <T> T castOrThrow(Class<T> type) {
        T t = castNullable(type);
        if (t == null) {
            if (!isSyncHandler() && !isValueHandler()) {
                throw new IllegalStateException("Empty sync handler or value can't be used for anything.");
            }
            String self = isSyncHandler() ? "sync handler" : "value";
            throw new IllegalStateException("Can't cast " + self + " of type '" + getClass().getSimpleName() +
                    "' to type '" + type.getSimpleName() + "'.");
        }
        return t;
    }

    /**
     * Returns if the containing value of this is of the given type. If this is not a value it will always return false.
     *
     * @param type expected value type
     * @return if the containing value of this is of the given type
     */
    default boolean isValueOfType(Class<?> type) {
        return false;
    }

    /**
     * @return if this is a sync handler (false if this represents null)
     */
    default boolean isSyncHandler() {
        return false;
    }

    /**
     * @return if this is a value handler (false if this represents null)
     */
    default boolean isValueHandler() {
        return false;
    }
}
