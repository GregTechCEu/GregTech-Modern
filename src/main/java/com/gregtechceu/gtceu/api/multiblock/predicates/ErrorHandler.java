package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;

@FunctionalInterface
public interface ErrorHandler {

    void appendError(PredicateContext context, BasePredicate failingPredicate);
}
