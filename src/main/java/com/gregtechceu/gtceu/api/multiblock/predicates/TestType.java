package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import java.util.Collection;
import java.util.List;

public enum TestType {

    GLOBAL_MIN,
    GLOBAL_MAX,
    SLICE_MIN,
    SLICE_MAX;

    /// @return {@code true}, if the holder does not have settings,
    /// or passes their settings according to the type
    /// @implNote The count of the holder WILL be incremented for glabal/slice max
    public boolean testAndIncrement(SettingsHolder<?> holder, PredicateContext ctx) {
        if (!holder.hasSettings()) return true;
        return switch (this) {
            case GLOBAL_MAX -> holder.testGlobalMax(ctx.incrementGlobalCount(holder));
            case SLICE_MAX -> holder.testSliceMax(ctx.incrementSliceCount(holder));
            default -> testCounts(holder, ctx);
        };
    }

    /// @return {@code true}, if the holder does not have settings,
    /// or passes their settings according to the type
    public boolean testCounts(SettingsHolder<?> holder, PredicateContext ctx) {
        if (!holder.hasSettings()) return true;
        if (isLayer() && !ctx.isCheckLayer()) return true;
        return switch (this) {
            case GLOBAL_MAX -> holder.testGlobalMax(ctx.getGlobalCount(holder));
            case SLICE_MAX -> holder.testSliceMax(ctx.getSliceCount(holder));
            case GLOBAL_MIN -> holder.testGlobalMin(ctx.getGlobalCount(holder));
            case SLICE_MIN -> holder.testSliceMin(ctx.getSliceCount(holder));
        };
    }

    /// @return {@code true}, if the holder does not have settings,
    /// or passes their settings according to the type
    /// @implNote The count of the holder WILL be incremented for glabal/slice max <br />
    /// Errors will be logged to the predicate context on failure
    public boolean testWithError(SettingsHolder<?> holder, PredicateContext ctx) {
        if (testAndIncrement(holder, ctx)) return true;
        if (holder instanceof MultiPredicate mp) appendError(mp, ctx);
        else appendError((BasePredicate) holder, ctx);
        return false;
    }

    private boolean isLayer() {
        return this == SLICE_MAX || this == SLICE_MIN;
    }

    public void appendError(MultiPredicate predicate, PredicateContext ctx) {
        List<BlockInfo> candidates = predicate.getCandidates().stream().flatMap(Collection::stream).toList();
        appendError(predicate, candidates, ctx);
    }

    public void appendError(BasePredicate predicate, PredicateContext ctx) {
        appendError(predicate, predicate.getCandidates(), ctx);
    }

    private void appendError(SettingsHolder<?> holder, List<BlockInfo> candidates, PredicateContext ctx) {
        int count = getCount(holder, ctx);
        ctx.appendError(switch (this) {
            case GLOBAL_MAX -> SinglePredicateError.maxCount(holder, candidates, count);
            case SLICE_MAX -> SinglePredicateError.maxLayerCount(holder, candidates, count);
            case GLOBAL_MIN -> SinglePredicateError.minCount(holder, candidates, count);
            case SLICE_MIN -> SinglePredicateError.minLayerCount(holder, candidates, count);
        });
    }

    public int getCount(SettingsHolder<?> holder, PredicateContext ctx) {
        return switch (this) {
            case GLOBAL_MAX, GLOBAL_MIN -> ctx.getGlobalCount(holder);
            case SLICE_MAX, SLICE_MIN -> ctx.getSliceCount(holder);
        };
    }
}
