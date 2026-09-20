package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.TestType;

import java.util.List;

public class OrPredicate extends MultiPredicate {

    public OrPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir) {
        super(Logic.OR, children, predicates, hasAir);
    }

    @Override
    protected boolean testGlobalMin(PredicateContext ctx) {
        boolean result = TestType.GLOBAL_MIN.testCounts(this, ctx);
        for (BasePredicate predicate : predicates()) {
            result |= predicate.testGlobalMin(ctx);
        }
        for (MultiPredicate child : children()) {
            result |= child.testGlobalMin(ctx);
        }
        return result;
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        boolean result = TestType.SLICE_MIN.testCounts(this, ctx);
        for (BasePredicate predicate : predicates()) {
            result |= predicate.testSliceMin(ctx);
        }
        for (MultiPredicate child : children()) {
            result |= child.testSliceMin(ctx);
        }
        return result;
    }
}
