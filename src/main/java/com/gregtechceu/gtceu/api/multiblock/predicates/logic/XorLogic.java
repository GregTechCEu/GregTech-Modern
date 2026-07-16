package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

public class XorLogic extends BaseLogic {

    private BasePredicate passedPredicate;

    public XorLogic(MultiPredicate rootPredicate) {
        super(rootPredicate);
    }

    @Override
    public void reset() {
        this.passedPredicate = null;
    }

    @Override
    public boolean test(PredicateContext ctx) {
        if (passedPredicate != null) {
            return passedPredicate.testLimited(ctx) || ctx.error(PatternStringError.literal("XOR error"));
        }
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate.testLimited(ctx) && this.passedPredicate == null) {
                this.passedPredicate = predicate;
                return true;
            }
        }
        return noneValid || ctx.error(PatternStringError.literal("XOR error"));
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testGlobalMin(ctx)) {
            return ctx.error(PatternStringError.literal("XOR error"));
        }
        if (!global) return true;
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate != passedPredicate && ctx.getGlobalCount(predicate) > 0) {
                return ctx.error(PatternStringError.literal("XOR error"));
            }
        }
        return true;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testSliceMin(ctx)) {
            return ctx.error(PatternStringError.literal("XOR error"));
        }
        if (global) return true;
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate != passedPredicate && ctx.getSliceCount(predicate) > 0) {
                return ctx.error(PatternStringError.literal("XOR error"));
            }
        }
        return true;
    }

    @Override
    public MultiPredicate.Logic getType() {
        return MultiPredicate.Logic.XOR;
    }
}
