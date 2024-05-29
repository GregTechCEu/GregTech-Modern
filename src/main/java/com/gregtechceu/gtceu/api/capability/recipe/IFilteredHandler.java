package com.gregtechceu.gtceu.api.capability.recipe;

import java.util.Comparator;
import java.util.function.Predicate;

public interface IFilteredHandler<K> extends Predicate<K> {

    Comparator<IFilteredHandler<?>> PRIORITY_COMPARATOR = Comparator.comparingInt(IFilteredHandler::getPriority);
    int NO_PRIORITY = Integer.MIN_VALUE;

    /**
     * Test an ingredient for filtering & priority.
     * 
     * @param ingredient the ingredient
     * @return {@code true} if the input argument matches the predicate,
     *         otherwise {@code false}
     */
    @Override
    default boolean test(K ingredient) {
        return true;
    }

    /**
     * The priority of this recipe handler.
     */
    default int getPriority() {
        return NO_PRIORITY;
    }
}
