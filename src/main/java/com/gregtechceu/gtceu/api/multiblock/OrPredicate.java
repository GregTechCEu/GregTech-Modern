package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import java.util.List;

public class OrPredicate extends MultiPredicate {

    public OrPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir) {
        super(Logic.OR, children, predicates, hasAir);
    }

    @Override
    protected boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : predicates()) {
            if (predicate.testGlobalMin(ctx)) {
                return true;
            }
        }
        for (MultiPredicate child : children()) {
            if (child.testGlobalMin(ctx)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : predicates()) {
            if (predicate.testSliceMin(ctx)) {
                return true;
            }
        }
        for (MultiPredicate child : children()) {
            if (child.testSliceMin(ctx)) {
                return true;
            }
        }
        return false;
    }
}
