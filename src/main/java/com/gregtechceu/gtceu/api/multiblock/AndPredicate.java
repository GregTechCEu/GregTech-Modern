package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateSettings;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AndPredicate extends MultiPredicate {

    public AndPredicate(List<MultiPredicate> children, List<BasePredicate> predicates, boolean hasAir,
                        @Nullable PredicateSettings settings) {
        super(Logic.AND, children, predicates, hasAir, settings);
    }

    @Override
    protected boolean testGlobalMin(PredicateContext ctx) {
        for (BasePredicate predicate : predicates()) {
            if (!predicate.testGlobalMin(ctx)) {
                return false;
            }
        }
        for (MultiPredicate child : children()) {
            if (!child.testGlobalMin(ctx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        for (BasePredicate predicate : predicates()) {
            if (!predicate.testSliceMin(ctx)) {
                return false;
            }
        }
        for (MultiPredicate child : children()) {
            if (!child.testSliceMin(ctx)) {
                return false;
            }
        }
        return true;
    }
}
