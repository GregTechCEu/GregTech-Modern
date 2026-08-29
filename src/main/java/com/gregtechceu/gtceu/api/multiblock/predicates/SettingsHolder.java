package com.gregtechceu.gtceu.api.multiblock.predicates;

import java.util.function.UnaryOperator;

public interface SettingsHolder<S extends SettingsHolder<S>> {

    // getters
    PredicateSettings getSettings();

    default int getPriority() {
        return getSettings().priority();
    }

    default int getMinCount() {
        return getSettings().minCount();
    }

    default int getMaxCount() {
        return getSettings().maxCount();
    }

    default int getMinSliceCount() {
        return getSettings().minSliceCount();
    }

    default int getMaxSliceCount() {
        return getSettings().maxSliceCount();
    }

    default int getPreviewCount() {
        return getSettings().previewCount();
    }

    default boolean isRenderFormedDisabled() {
        return getSettings().disableRenderFormed();
    }

    // mutate only
    void setSettings(PredicateSettings settings);

    /// mutates this object with the configured setting
    default void updateSettings(UnaryOperator<PredicateSettings> configurator) {
        setSettings(configurator.apply(getSettings()));
    }

    default void setPriority(int priority) {
        updateSettings(s -> s.withPriority(priority));
    }

    default void setMinCount(int minCount) {
        updateSettings(s -> s.withMinCount(minCount));
    }

    default void setMaxCount(int maxCount) {
        updateSettings(s -> s.withMaxCount(maxCount));
    }

    default void setMinSliceCount(int minSliceCount) {
        updateSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    default void setMaxSliceCount(int maxSliceCount) {
        updateSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    default void setPreviewCount(int previewCount) {
        updateSettings(s -> s.withPreviewCount(previewCount));
    }

    default void setDisableRenderFormed(boolean disableRenderFormed) {
        updateSettings(s -> s.withDisableRenderFormed(disableRenderFormed));
    }

    // copy and mutate
    /// @return a copy with these settings applied
    S withSettings(UnaryOperator<PredicateSettings> configurator);

    default S withPriority(int priority) {
        return withSettings(s -> s.withPriority(priority));
    }

    default S withMinCount(int minCount) {
        return withSettings(s -> s.withMinCount(minCount));
    }

    default S withMaxCount(int maxCount) {
        return withSettings(s -> s.withMaxCount(maxCount));
    }

    default S withMinSliceCount(int minSliceCount) {
        return withSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    default S withMaxSliceCount(int maxSliceCount) {
        return withSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    default S withPreviewCount(int previewCount) {
        return withSettings(s -> s.withPreviewCount(previewCount));
    }

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
}
