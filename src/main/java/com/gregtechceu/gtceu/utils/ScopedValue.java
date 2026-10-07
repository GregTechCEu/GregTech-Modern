package com.gregtechceu.gtceu.utils;

import org.jetbrains.annotations.Nullable;

/**
 * "recreations" of Java 25 scoped values using {@link AutoCloseable AutoCloseables}.
 */
public abstract class ScopedValue implements AutoCloseable {

    /**
     * Scoped object value. Resets to {@code null} when exiting scope.
     * 
     * @param <T> The type of the object.
     */
    public static final class Object<T> extends ScopedValue {

        private final ThreadLocal<@Nullable T> value;

        public Object() {
            this(null);
        }

        public Object(@Nullable T initialValue) {
            this.value = ThreadLocal.withInitial(() -> initialValue);
        }

        /**
         * {@return Current value in this scope}
         */
        public @Nullable T getValue() {
            return this.value.get();
        }

        /**
         * Set {@code current} to {@code value} within this scope.
         * 
         * @return this
         */
        public Object<T> with(T value) {
            this.value.set(value);
            return this;
        }

        @Override
        public void close() {
            this.value.remove();
        }
    }

    /**
     * Scoped boolean value. Resets to {@link #initialValue} when exiting scope.
     */
    public static final class Boolean extends ScopedValue {

        private final boolean initialValue;

        private final ThreadLocal<java.lang.Boolean> value;

        public Boolean() {
            this(false);
        }

        public Boolean(boolean initialValue) {
            this.initialValue = initialValue;
            this.value = ThreadLocal.withInitial(() -> this.initialValue);
        }

        /**
         * {@return Current value in this scope}
         */
        public boolean isActive() {
            return this.value.get();
        }

        /**
         * Set {@code current} to {@code value} within this scope.
         * 
         * @return this
         */
        public Boolean with(boolean value) {
            this.value.set(value);
            return this;
        }

        /**
         * Shortcut method that sets {@code current} to {@code !initial}
         * 
         * @return this
         */
        public Boolean setActive() {
            return with(!this.initialValue);
        }

        @Override
        public void close() {
            this.value.remove();
        }
    }
}
