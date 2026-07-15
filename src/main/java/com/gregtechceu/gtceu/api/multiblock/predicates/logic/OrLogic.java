package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

public class OrLogic extends BaseLogic {

    public OrLogic(MultiPredicate rootPredicate) {
        super(rootPredicate);
    }

    @Override
    public boolean test(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.testLimited(ctx)) {
                return true;
            }
        }
        return ctx.error(PatternStringError.literal("OR error"));
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.testGlobalMin(ctx)) {
                return true;
            }
        }
        return ctx.error(PatternStringError.literal("OR error"));
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.testSliceMin(ctx)) {
                return true;
            }
        }
        return ctx.error(PatternStringError.literal("OR error"));
    }

    @Override
    public MultiPredicate.Logic getType() {
        return MultiPredicate.Logic.OR;
    }
}
