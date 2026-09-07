package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import java.util.List;

public class OrPredicate extends MultiPredicate {

    public OrPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir) {
        super(Logic.OR, children, predicates, hasAir);
    }

    @Override
    protected boolean testGlobalMin(PredicateContext ctx) {
        boolean result = false;
        for (BasePredicate predicate : predicates()) {
            if (predicate.testGlobalMin(ctx)) {
                result = true;
                break;
            }
        }
        if (!result) {
            for (MultiPredicate child : children()) {
                if (child.testGlobalMin(ctx)) {
                    result = true;
                    break;
                }
            }
        }
        if (result) {
            result = TestType.GLOBAL_MIN.testSettings(this, ctx);
        }
        return result;
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        boolean result = false;
        for (BasePredicate predicate : predicates()) {
            if (predicate.testSliceMin(ctx)) {
                result = true;
                break;
            }
        }
        if (!result) {
            for (MultiPredicate child : children()) {
                if (child.testSliceMin(ctx)) {
                    result = true;
                    break;
                }
            }
        }
        if (result) {
            result = TestType.SLICE_MIN.testSettings(this, ctx);
        }
        return result;
    }
}
