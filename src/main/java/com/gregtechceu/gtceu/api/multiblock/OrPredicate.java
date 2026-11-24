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
        boolean self = TestType.GLOBAL_MIN.testCounts(this, ctx);
        if (predicates().isEmpty() && children().isEmpty()) return self;
        boolean any = false;
        for (BasePredicate predicate : predicates()) {
            any |= predicate.testGlobalMin(ctx);
        }
        for (MultiPredicate child : children()) {
            any |= child.testGlobalMin(ctx);
        }
        return self && any;
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        boolean self = TestType.SLICE_MIN.testCounts(this, ctx);
        if (predicates().isEmpty() && children().isEmpty()) return self;
        boolean any = false;
        for (BasePredicate predicate : predicates()) {
            any |= predicate.testSliceMin(ctx);
        }
        for (MultiPredicate child : children()) {
            any |= child.testSliceMin(ctx);
        }
        return self && any;
    }
}
