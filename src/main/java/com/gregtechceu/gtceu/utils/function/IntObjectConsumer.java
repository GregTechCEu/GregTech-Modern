package com.gregtechceu.gtceu.utils.function;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

/**
 * A type-specific {@link BinaryOperator}; provides methods operating both on objects and on
 * primitives.
 *
 * @see BinaryOperator
 * @since 8.5.0
 */
@FunctionalInterface
public interface IntObjectConsumer<T> extends BiConsumer<Integer, T> {

    /**
     * Computes the operator on the given inputs.
     *
     * @param t the first input.
     * @param u the second input.
     * @return the output of the operator on the given inputs.
     */
    void accept(int t, T u);

    /**
     * {@inheritDoc}
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    @Override
    default void accept(Integer integer, T u) {
        accept((int) integer, u);
    }
}
