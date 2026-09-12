package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;

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
        return switch (this) {
            case GLOBAL_MAX -> holder.testGlobalMax(ctx.getGlobalCount(holder));
            case SLICE_MAX -> holder.testSliceMax(ctx.getSliceCount(holder));
            case GLOBAL_MIN -> holder.testGlobalMin(ctx.getGlobalCount(holder));
            case SLICE_MIN -> holder.testSliceMin(ctx.getSliceCount(holder));
        };
    }
}
