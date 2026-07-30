package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseLogic {

    protected final MultiPredicate rootPredicate;
    @Getter
    private final MultiPredicate.Logic type;
    @Setter
    protected boolean global = true; // used for XOR, AND

    public BaseLogic(MultiPredicate rootPredicate, MultiPredicate.Logic type) {
        this.rootPredicate = rootPredicate;
        this.type = type;
    }

    public void reset() {}

    /// check max counts plus any additional logic
    public abstract boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context);

    public abstract boolean testGlobalMin(PredicateContext ctx);

    public abstract boolean testSliceMin(PredicateContext ctx);

    @Override
    public String toString() {
        return getType().name();
    }

    public void predicatePassed(BasePredicate predicate) {}
}
