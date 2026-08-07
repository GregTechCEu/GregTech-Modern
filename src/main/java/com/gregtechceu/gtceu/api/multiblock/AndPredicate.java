package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import java.util.List;

public class AndPredicate extends MultiPredicate {

    public AndPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir) {
        super(Logic.AND, children, predicates, hasAir);
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : predicates()) {
            if (!predicate.testGlobalMin(ctx)) {
                return false;
            }
        }
        for (MultiPredicate child : children()) {
            if (!child.testGlobalMin(ctx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : predicates()) {
            if (!predicate.testSliceMin(ctx)) {
                return false;
            }
        }
        for (MultiPredicate child : children()) {
            if (!child.testSliceMin(ctx)) {
                return false;
            }
        }
        return true;
    }
}
