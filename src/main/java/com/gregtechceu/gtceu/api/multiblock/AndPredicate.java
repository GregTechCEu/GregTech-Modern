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
        boolean result = true;
        for (BasePredicate predicate : predicates()) {
            if (!predicate.testGlobalMin(ctx)) {
                result = false;
                break;
            }
        }
        if (result) {
            for (MultiPredicate child : children()) {
                if (!child.testGlobalMin(ctx)) {
                    result = false;
                    break;
                }
            }
        }
        if (result) {
            result = !hasSettings() || TestType.GLOBAL_MIN.testSettings(this, ctx);
        }
        return result;
    }

    @Override
    protected boolean testSliceMin(PredicateContext ctx) {
        boolean result = true;
        for (BasePredicate predicate : predicates()) {
            if (!predicate.testSliceMin(ctx)) {
                result = false;
                break;
            }
        }
        if (result) {
            for (MultiPredicate child : children()) {
                if (!child.testSliceMin(ctx)) {
                    result = false;
                    break;
                }
            }
        }
        if (result) {
            result = !hasSettings() || TestType.SLICE_MIN.testSettings(this, ctx);
        }
        return result;
    }
}
