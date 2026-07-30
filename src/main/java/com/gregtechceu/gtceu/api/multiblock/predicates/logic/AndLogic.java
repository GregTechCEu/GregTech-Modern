package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

public class AndLogic extends BaseLogic {

    public AndLogic(MultiPredicate rootPredicate) {
        super(rootPredicate, MultiPredicate.Logic.AND);
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (!predicate.testGlobalMin(ctx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (!predicate.testSliceMin(ctx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (!predicate.testGlobalMax(context) || !predicate.testSliceMax(context)) {
                // error?
                return false;
            }
        }
        return true;
    }
}
