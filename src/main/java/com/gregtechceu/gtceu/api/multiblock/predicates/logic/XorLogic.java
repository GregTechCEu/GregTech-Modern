package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

public class XorLogic extends BaseLogic {

    private BasePredicate passedPredicate;
    /// {@code true} if any base predicate have a min count of 0 or -1,
    /// meaning that it is possible that no predicates may be present in the multi.
    protected boolean noneValid;

    public XorLogic(MultiPredicate rootPredicate) {
        super(rootPredicate);
    }

    @Override
    public void reset() {
        super.reset();
        this.passedPredicate = null;
    }

    public void onPredicateAdded() {
        super.onPredicateAdded();
        this.noneValid = isNoneValid(this.rootPredicate);
    }

    private static boolean isNoneValid(MultiPredicate rootPredicate) {
        boolean noneValid = false;
        for (BasePredicate predicate : rootPredicate) {
            if (predicate instanceof MultiPredicate.CompactedPredicate cp) {
                noneValid |= isNoneValid(cp.expand());
            } else {
                noneValid |= predicate.getMinCount() <= 0 && predicate.getMinSliceCount() <= 0;
            }
        }
        return noneValid;
    }

    @Override
    public boolean test(PredicateContext ctx) {
        int passed = 0;
        for (BasePredicate predicate : this.rootPredicate) {
            boolean result = predicate.testLimited(ctx);
            if (result && this.passedPredicate == null) {
                this.passedPredicate = predicate;
            }
            if (result) passed++;
        }
        return passed > 0 || ctx.error(PatternStringError.literal("XOR error"));
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
