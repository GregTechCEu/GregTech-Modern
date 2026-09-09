package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateSettings;

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
    }

    @Override
    protected void onSettingsChanged() {
        this.noneValid = isNoneValid(this);
    }

    @Override
    protected PredicateResult onPredicateMatched(PredicateResult result, PredicateContext context) {
        if (result.match() == null) return result;
        if (result.isTop(this)) {
            if (this.passedPredicate != null && !this.passedPredicate.is(result.match())) {
                xorError(context, result.match(), this.passedPredicate);
                return result.setFailed();
            }
            if (this.passedPredicate == null) {
                this.passedPredicate = ofPredicate(result.match());
            }
        } else {
            MultiPredicate bottom = Objects.requireNonNull(result.getBottom());
            if (this.passedPredicate != null && !this.passedPredicate.is(bottom)) {
                xorError(context, result.match(), this.passedPredicate);
                return result.setFailed();
            }
            if (this.passedPredicate == null) {
                this.passedPredicate = ofChild(bottom);
            }
        }
        return result;
    }

    @Override
    protected boolean testGlobalMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        return passedPredicate != null && passedPredicate.testGlobalMin(ctx);
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        if (passedPredicate == null && noneValid) return true;
        return passedPredicate != null && passedPredicate.testSliceMin(ctx);
    }

    @Override
    public void resetLogic() {
        super.resetLogic();
        this.passedPredicate = null;
    }

    private static void xorError(PredicateContext context, BasePredicate predicate, PassedPredicate passedPredicate) {
        MutableComponent found = Component.literal(predicate + " present in multiblock");
        Component passed = passedPredicate.toComponent();
        MutableComponent expected = Component
                .literal("expected only: " + passed.getString());
        context.appendError(
                PatternStringError.literal("XOR error\n" + found.getString() + "\n" + expected.getString()));
        context.skipFlipCheck();
    }

    private static boolean isNoneValid(MultiPredicate multiPredicate) {
        PredicateSettings settings = multiPredicate.getSettings();
        if (settings != null && !settings.isNoneValid()) return false;

        for (BasePredicate predicate : multiPredicate.predicates()) {
            if (predicate.getSettings().isNoneValid()) {
                return true;
            }
        }
        for (MultiPredicate child : multiPredicate.children()) {
            if (isNoneValid(child)) {
                return true;
            }
        }
        return false;
    }

    private static PassedPredicate ofPredicate(BasePredicate predicate) {
        return new PassedPredicate(predicate, null);
    }

    private static PassedPredicate ofChild(MultiPredicate predicate) {
        return new PassedPredicate(null, predicate);
    }

    // this needs to hold both base predicate or passing children
    private record PassedPredicate(@Nullable BasePredicate predicate, @Nullable MultiPredicate multiPredicate) {

        public boolean testGlobalMin(PredicateContext ctx) {
            if (this.predicate != null) {
                return this.predicate.testGlobalMin(ctx);
            } else if (this.multiPredicate != null) {
                return this.multiPredicate.testGlobalMin(ctx);
            }
            throw new IllegalStateException();
        }

        public boolean testSliceMin(PredicateContext ctx) {
            if (predicate != null) {
                return predicate.testSliceMin(ctx);
            } else if (multiPredicate != null) {
                return multiPredicate.testSliceMin(ctx);
            }
            throw new IllegalStateException();
        }

        public boolean is(BasePredicate predicate) {
            if (this.multiPredicate != null) return false;
            return Objects.requireNonNull(this.predicate) == predicate;
        }

        public boolean is(MultiPredicate multiPredicate) {
            if (this.predicate != null) return false;
            return Objects.requireNonNull(this.multiPredicate) == multiPredicate;
        }

        public Component toComponent() {
            if (this.predicate != null) {
                return Component.literal(this.predicate.toString());
            } else if (this.multiPredicate != null) {
                return Component.literal(this.multiPredicate.toString());
            }
            return Component.empty();
        }
    }
}
