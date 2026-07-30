package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.CompactedPredicate;

public class XorLogic extends BaseLogic {

    private BasePredicate passedPredicate;
    /// {@code true} if any base predicate have a min count of 0 or -1,
    /// meaning that it is possible that no predicates may be present in the multi.
    protected boolean noneValid;

    public XorLogic(MultiPredicate rootPredicate) {
        super(rootPredicate, MultiPredicate.Logic.XOR);
        this.noneValid = isNoneValid(this.rootPredicate);
    }

    @Override
    public void reset() {
        this.passedPredicate = null;
    }

    private static boolean isNoneValid(MultiPredicate rootPredicate) {
        boolean noneValid = false;
        for (BasePredicate predicate : rootPredicate) {
            if (predicate instanceof CompactedPredicate compacted) {
                noneValid |= isNoneValid(compacted.expand());
            } else {
                noneValid |= predicate.getMinCount() <= 0 && predicate.getMinSliceCount() <= 0;
            }
        }
        return noneValid;
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testGlobalMin(ctx)) {
            ctx.appendError(PatternStringError.literal("need one of: " + rootPredicate));
            return false;
        }
        // if (!global) return true;
        return true;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testSliceMin(ctx)) {
            ctx.appendError(PatternStringError.literal("need one of: " + rootPredicate));
            return false;
        }
        // if (global) return true;
        if (!global) reset();
        return true;
    }

    @Override
    public boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context) {
        context.setStage(PredicateContext.PredicateStage.GLOBAL_MAX); // set stage early to avoid flip check
        if (passedPredicate != this.passedPredicate) {
            // todo prettier error
            context.appendError(PatternStringError.literal(passedPredicate + " present in multi"));
            return false;
        }
        return this.passedPredicate.testGlobalMax(context) && this.passedPredicate.testSliceMax(context);
    }

    @Override
    public void predicatePassed(BasePredicate predicate) {
        if (this.passedPredicate == null) {
            this.passedPredicate = predicate;
        }
    }
}
