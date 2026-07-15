package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

public abstract class BaseLogic {

    protected final MultiPredicate rootPredicate;

    public BaseLogic(MultiPredicate rootPredicate) {
        this.rootPredicate = rootPredicate;
    }

    public void reset() {}

    public abstract boolean test(PredicateContext ctx);

    public abstract boolean testGlobalMin(PredicateContext ctx);

    public abstract boolean testSliceMin(PredicateContext ctx);

    public abstract MultiPredicate.Logic getType();
}
