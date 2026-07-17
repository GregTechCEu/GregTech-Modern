package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;

public class AndLogic extends BaseLogic {

    public AndLogic(MultiPredicate rootPredicate) {
        super(rootPredicate);
    }

    @Override
    public boolean test(PredicateContext ctx) {
        int passed = 0;
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.test(ctx)) {
                passed++;
                if (predicate.testGlobalMax(ctx) && predicate.testSliceMax(ctx)) {
                    continue; // fully passed
                }
            }

            // count manually
            if (failedMaxCount(ctx, predicate, true) || failedMaxCount(ctx, predicate, false))
                return ctx.error(PatternStringError.literal("AND error"));
            passed++;
            // continue...
        }
        return passed > 0;
    }

    private static boolean failedMaxCount(PredicateContext ctx, BasePredicate predicate, boolean global) {
        int count = global ? ctx.getGlobalCount(predicate) : ctx.getSliceCount(predicate);
        return global ? !predicate.testGlobalMax(count) : !predicate.testSliceMax(count);
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
    public MultiPredicate.Logic getType() {
        return MultiPredicate.Logic.AND;
    }
}
