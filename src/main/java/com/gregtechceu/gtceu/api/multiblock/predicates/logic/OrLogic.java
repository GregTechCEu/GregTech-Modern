package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

public class OrLogic extends BaseLogic {

    public OrLogic(MultiPredicate rootPredicate) {
        super(rootPredicate, MultiPredicate.Logic.OR);
    }

    @Override
    public boolean testMaxCount(BasePredicate passedPredicate, PredicateContext ctx) {
        return passedPredicate.testGlobalMax(ctx) && passedPredicate.testSliceMax(ctx);
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.testGlobalMin(ctx)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.testSliceMin(ctx)) {
                return true;
            }
        }
        return false;
    }
}
