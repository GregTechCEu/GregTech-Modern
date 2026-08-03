package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class XorPredicate extends MultiPredicate {

    private @Nullable BasePredicate passedPredicate;
    /// {@code true} if any base predicate have a min count of 0 or -1,
    /// meaning that it is possible that no predicates may be present in the multi.
    protected boolean noneValid;

    public XorPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir) {
        super(Logic.XOR, children, predicates, hasAir);
        this.noneValid = isNoneValid(this);
    }

    private static boolean isNoneValid(MultiPredicate rootPredicate) {
        boolean noneValid = false;
        for (BasePredicate predicate : rootPredicate.expand()) {
            noneValid |= predicate.getMinCount() <= 0 && predicate.getMinSliceCount() <= 0;
        }
        return noneValid;
    }

    @Override
    public @Nullable BasePredicate getPredicateAtPos(PredicateContext context) {
        BasePredicate predicate = super.getPredicateAtPos(context);
        if (predicate != null && this.passedPredicate == null) {
            this.passedPredicate = predicate;
        }
        return predicate;
    }

    @Override
    public boolean testGlobalMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testGlobalMin(ctx)) {
            ctx.appendError(PatternStringError.literal("need one of: " + this));
            return false;
        }
        // if (!global) return true;
        return true;
    }

    @Override
    public boolean testSliceMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        if (passedPredicate == null || !passedPredicate.testSliceMin(ctx)) {
            ctx.appendError(PatternStringError.literal("need one of: " + this));
            return false;
        }
        // if (global) return true;
        // if (!global) resetLogic();
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
    public void resetLogic() {
        super.resetLogic();
        this.passedPredicate = null;
    }
}
