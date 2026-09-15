package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.TestType;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record PredicateResult(@Nullable BasePredicate match, List<MultiPredicate> parents) {

    private static final PredicateResult NO_MATCH = new PredicateResult(null, List.of());

    // if failed, return early with no match
    private static final PredicateResult FAILED = new PredicateResult(null, List.of());

    public static PredicateResult noMatch() {
        return NO_MATCH;
    }

    public static PredicateResult failed() {
        return FAILED;
    }

    public static PredicateResult of(BasePredicate match, MultiPredicate parent) {
        return new PredicateResult(Objects.requireNonNull(match), List.of(Objects.requireNonNull(parent)));
    }

    public PredicateResult appendParent(MultiPredicate parent) {
        if (hasFailed()) return this;
        ArrayList<MultiPredicate> parents = new ArrayList<>(this.parents);
        parents.add(parent);
        return new PredicateResult(this.match, parents);
    }

    public boolean hasFailed() {
        return this == FAILED;
    }

    public boolean hasMatched() {
        return this.match != null;
    }

    // return true if passed, else false
    public boolean testMaxCount(PredicateContext context) {
        Objects.requireNonNull(this.match, "matched base predicate must not be null");

        context.setStage(PredicateContext.PredicateStage.GLOBAL_MAX);
        if (!testParents(TestType.GLOBAL_MAX, context)) {
            return false;
        }

        context.setStage(PredicateContext.PredicateStage.SLICE_MAX);
        return testParents(TestType.SLICE_MAX, context);
    }

    public boolean contains(MultiPredicate predicate) {
        return parents.contains(predicate);
    }

    public boolean isTop(MultiPredicate predicate) {
        if (parents.isEmpty()) return false;
        return Objects.equals(getTop(), predicate);
    }

    public @Nullable MultiPredicate getTop() {
        if (parents.isEmpty()) return null;
        return parents.get(0);
    }

    public @Nullable MultiPredicate getBottom() {
        if (parents.isEmpty()) return null;
        return parents.get(parents.size() - 1);
    }

    // go up the parent chain to test settings of parents
    // if any return false, fail
    private boolean testParents(TestType type, PredicateContext context) {
        Objects.requireNonNull(this.match, "matched base predicate must not be null");
        if (!type.testWithError(this.match, context)) {
            return false;
        }
        for (MultiPredicate parent : this.parents) {
            if (!type.testWithError(parent, context)) {
                return false;
            }
        }
        return true;
    }
}
