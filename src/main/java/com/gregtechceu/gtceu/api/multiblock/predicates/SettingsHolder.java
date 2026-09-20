package com.gregtechceu.gtceu.api.multiblock.predicates;

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

    // copy and mutate
    /// @return a copy with these settings applied
    S withSettings(UnaryOperator<PredicateSettings> configurator);

    /// @return a copy of this object with the given priority
    @CheckReturnValue
    default S setPriority(int priority) {
        return withSettings(s -> s.withPriority(priority));
    }

    /// @return a copy of this object with the given min global count
    @CheckReturnValue
    default S setMinCount(int minCount) {
        return withSettings(s -> s.withMinCount(minCount));
    }

    /// @return a copy of this object with the given max global count
    @CheckReturnValue
    default S setMaxCount(int maxCount) {
        return withSettings(s -> s.withMaxCount(maxCount));
    }

    /// @return a copy of this object with the given min slice count
    @CheckReturnValue
    default S setMinSliceCount(int minSliceCount) {
        return withSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    /// @return a copy of this object with the given max slice count
    @CheckReturnValue
    default S setMaxSliceCount(int maxSliceCount) {
        return withSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    /// @return a copy of this object with the given preview count
    @CheckReturnValue
    default S setPreviewCount(int previewCount) {
        return withSettings(s -> s.withPreviewCount(previewCount));
    }

    /// @return a copy of this object with the given render form disabled
    @CheckReturnValue
    default S setDisableRenderFormed(boolean disableRenderFormed) {
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
}
