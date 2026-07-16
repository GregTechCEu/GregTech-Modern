package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

public class AndLogic extends BaseLogic {

    public AndLogic(MultiPredicate rootPredicate) {
        super(rootPredicate);
    }

    @Override
    public boolean test(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            boolean result = predicate.testLimited(ctx);
            if (result) continue;
            switch (ctx.getLastFailureReason()) {
                case SLICE_MAX, GLOBAL_MAX -> {
                    return ctx.error(PatternStringError.literal("AND error"));
                }
            }
        }
        return true;
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (!predicate.testGlobalMin(ctx)) {
                return ctx.error(PatternStringError.literal("AND error"));
            }
        }
        return true;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (!predicate.testSliceMin(ctx)) {
                return ctx.error(PatternStringError.literal("AND error"));
            }
        }
        return true;
    }

    @Override
    public MultiPredicate.Logic getType() {
        return MultiPredicate.Logic.AND;
    }
}
