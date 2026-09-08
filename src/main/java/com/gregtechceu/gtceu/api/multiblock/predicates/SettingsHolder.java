package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.function.UnaryOperator;

public interface SettingsHolder<S extends SettingsHolder<S>> extends Comparable<SettingsHolder<S>> {

    // getters
    PredicateSettings getSettings();

    boolean hasSettings();

    default int getPriority() {
        return hasSettings() ? getSettings().priority() : PredicateSettings.MIN_PRIORITY;
    }

    default int getMinCount() {
        return hasSettings() ? getSettings().minCount() : -1;
    }

    default int getMaxCount() {
        return hasSettings() ? getSettings().maxCount() : -1;
    }

    default int getMinSliceCount() {
        return hasSettings() ? getSettings().minSliceCount() : -1;
    }

    default int getMaxSliceCount() {
        return hasSettings() ? getSettings().maxSliceCount() : -1;
    }

    default int getPreviewCount() {
        return hasSettings() ? getSettings().previewCount() : -1;
    }

    default boolean isRenderFormedDisabled() {
        return hasSettings() && getSettings().disableRenderFormed();
    }

    // mutate only
    @ApiStatus.Internal
    void setSettings(PredicateSettings settings);

    /// mutates this object with the configured setting
    @ApiStatus.Internal
    default void updateSettings(UnaryOperator<PredicateSettings> configurator) {
        setSettings(configurator.apply(getSettings()));
    }

    /// mutates this object with the given priority
    @ApiStatus.Internal
    default void setPriority(int priority) {
        updateSettings(s -> s.withPriority(priority));
    }

    /// mutates this object with the given min global count
    @ApiStatus.Internal
    default void setMinCount(int minCount) {
        updateSettings(s -> s.withMinCount(minCount));
    }

    /// mutates this object with the given max global count
    @ApiStatus.Internal
    default void setMaxCount(int maxCount) {
        updateSettings(s -> s.withMaxCount(maxCount));
    }

    /// mutates this object with the given min slice count
    @ApiStatus.Internal
    default void setMinSliceCount(int minSliceCount) {
        updateSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    /// mutates this object with the given max slice count
    @ApiStatus.Internal
    default void setMaxSliceCount(int maxSliceCount) {
        updateSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    /// mutates this object with the given preview count
    @ApiStatus.Internal
    default void setPreviewCount(int previewCount) {
        updateSettings(s -> s.withPreviewCount(previewCount));
    }

    /// mutates this object with the given render formed disabled
    @ApiStatus.Internal
    default void setDisableRenderFormed(boolean disableRenderFormed) {
        updateSettings(s -> s.withDisableRenderFormed(disableRenderFormed));
    }

    // copy and mutate
    /// @return a copy with these settings applied
    S withSettings(UnaryOperator<PredicateSettings> configurator);

    /// @return a copy of this object with the given priority
    @CheckReturnValue
    default S withPriority(int priority) {
        return withSettings(s -> s.withPriority(priority));
    }

    /// @return a copy of this object with the given min global count
    @CheckReturnValue
    default S withMinCount(int minCount) {
        return withSettings(s -> s.withMinCount(minCount));
    }

    /// @return a copy of this object with the given max global count
    @CheckReturnValue
    default S withMaxCount(int maxCount) {
        return withSettings(s -> s.withMaxCount(maxCount));
    }

    /// @return a copy of this object with the given min slice count
    @CheckReturnValue
    default S withMinSliceCount(int minSliceCount) {
        return withSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    /// @return a copy of this object with the given max slice count
    @CheckReturnValue
    default S withMaxSliceCount(int maxSliceCount) {
        return withSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    /// @return a copy of this object with the given preview count
    @CheckReturnValue
    default S withPreviewCount(int previewCount) {
        return withSettings(s -> s.withPreviewCount(previewCount));
    }

    /// @return a copy of this object with the given render form disabled
    @CheckReturnValue
    default S withDisableRenderFormed(boolean disableRenderFormed) {
        return withSettings(s -> s.withDisableRenderFormed(disableRenderFormed));
    }

    // test methods
    /// simple test against global min count
    default boolean testGlobalMin(int count) {
        return getMinCount() == -1 || count >= getMinCount();
    }

    /// simple test against slice min count
    default boolean testSliceMin(int count) {
        return getMinSliceCount() == -1 || count >= getMinSliceCount();
    }

    /// simple test against global max count
    default boolean testGlobalMax(int count) {
        return getMaxCount() == -1 || count <= getMaxCount();
    }

    /// simple test against slice max count
    default boolean testSliceMax(int count) {
        return getMaxSliceCount() == -1 || count <= getMaxSliceCount();
    }

    @Override
    default int compareTo(SettingsHolder<S> o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

    enum TestType {

        GLOBAL_MIN,
        GLOBAL_MAX,
        SLICE_MIN,
        SLICE_MAX;

        /// @implNote The count of the holder WILL be incremented for glabal/slice max
        /// @return {@code true}, if the holder does not have settings,
        /// or passes their settings according to the type
        public boolean testAndIncrement(SettingsHolder<?> holder, PredicateContext ctx) {
            if (!holder.hasSettings()) return true;
            return switch (this) {
                case GLOBAL_MAX -> holder.getMaxCount() == -1 || holder.testGlobalMax(ctx.incrementGlobalCount(holder));
                case SLICE_MAX -> holder.getMaxSliceCount() == -1 || holder.testSliceMax(ctx.incrementSliceCount(holder));
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
}
