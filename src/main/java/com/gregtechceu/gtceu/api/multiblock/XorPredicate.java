package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class XorPredicate extends MultiPredicate {

    // this could be a base predicate or a child
    private @Nullable PassedPredicate passedPredicate;
    /// {@code true} if any base predicate have a min count of 0 or -1,
    /// meaning that it is possible that no predicates may be present in the multi.
    protected boolean noneValid;

    public XorPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir) {
        super(Logic.XOR, children, predicates, hasAir);
        this.noneValid = isNoneValid();
    }

    private boolean isNoneValid() {
        boolean noneValid = false;
        for (BasePredicate predicate : expand()) {
            noneValid |= predicate.getMinCount() <= 0 && predicate.getMinSliceCount() <= 0;
        }
        return noneValid;
    }

    @Override
    public @Nullable BasePredicate getPredicateAtPos(PredicateContext context) {
        for (BasePredicate predicate : predicates()) {
            if (predicate.test(context)) {
                setPassedPredicate(predicate, null);
                if (checkPredicate(predicate)) {
                    xorError(context, predicate);
                    // error
                    return null;
                }
                return predicate;
            }
        }
        for (MultiPredicate predicates : children()) {
            BasePredicate p = predicates.getPredicateAtPos(context);
            if (p != null) {
                setPassedPredicate(p, predicates);
                if (checkPredicate(p)) {
                    xorError(context, p);
                    // error
                    return null;
                }
                return p;
            }
        }
        if (!hasChildren()) {
            onError(context);
        }
        return null;
    }

    private void xorError(PredicateContext context, BasePredicate predicate) {
        MutableComponent found = Component.literal(predicate + " present in multiblock");
        MutableComponent expected = Component.literal("expected only: " + Objects.requireNonNull(passedPredicate).predicate());
        context.appendError(PatternStringError.literal("XOR error\n" + found.getString() + "\n" + expected.getString()));
        context.skipFlipCheck();
    }

    private boolean checkPredicate(BasePredicate predicate) {
        return this.passedPredicate != null && !this.passedPredicate.matches(predicate);
    }

    private void setPassedPredicate(BasePredicate predicate, @Nullable MultiPredicate multiPredicate) {
        if (this.passedPredicate == null) {
            this.passedPredicate = new PassedPredicate(predicate, multiPredicate);
        }
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
    public void resetLogic() {
        super.resetLogic();
        this.passedPredicate = null;
    }

    // this needs to hold both base predicate or passing children
    private record PassedPredicate(BasePredicate predicate, @Nullable MultiPredicate multiPredicate) {

        public boolean testGlobalMin(PredicateContext ctx) {
            return predicate.testGlobalMin(ctx);
        }

        public boolean testSliceMin(PredicateContext ctx) {
            return predicate.testSliceMin(ctx);
        }

        public boolean testMaxCount(PredicateContext context) {
            if (multiPredicate == null) {
                return predicate.testGlobalMax(context) && predicate.testSliceMax(context);
            } else {
                return multiPredicate.testMaxCount(predicate, context);
            }
        }

        public boolean matches(BasePredicate p) {
            return p == predicate && (multiPredicate == null || p.getParent() == multiPredicate);
        }
    }
}
