package com.gregtechceu.gtceu.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * "recreations" of Java 25 scoped values using {@link AutoCloseable AutoCloseables}.
 */
public abstract class ScopedValue implements AutoCloseable {

    /**
     * Scoped object value. Resets to {@code null} when exiting scope.
     * @param <T> The type of the object.
     */
    public static final class Object<T> extends ScopedValue {

        /**
         * Current value in this scope
         */
        @Getter
        private T current;

        /**
         * Set {@code current} to {@code value} within this scope.
         * @return this
         */
        public Object<T> with(T object) {
            this.current = object;
            return this;
        }

        @Override
        public void close() {
            this.current = null;
        }
    }

    /**
     * Scoped boolean value. Resets to {@link #initialValue} when exiting scope.
     */
    @RequiredArgsConstructor
    public static final class Boolean extends ScopedValue {

        private final boolean initialValue;

        /**
         * Current value in this scope
         */
        @Getter
        private boolean active;

        /**
         * Set {@code current} to {@code value} within this scope.
         * @return this
         */
        public Boolean with(boolean value) {
            this.active = value;
            return this;
        }

        /**
         * Shortcut method that sets {@code current} to {@code !initial}
         * @return this
         */
        public Boolean active() {
            return with(!this.initialValue);
        }

        @Override
        public void close() {
            this.active = this.initialValue;
        }
    }
}
