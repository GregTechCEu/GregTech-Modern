package com.gregtechceu.gtceu.api.multiblock.predicates;

import java.util.function.UnaryOperator;

public interface SettingsHolder {

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

    void setPriority(int priority);

    void setMinCount(int minCount);

    void setMaxCount(int maxCount);

    void setMinSliceCount(int minSliceCount);

    void setMaxSliceCount(int maxSliceCount);

    void setPreviewCount(int previewCount);

    void setDisableRenderFormed(boolean disableRenderFormed);


    // copy and mutate
    /// @return a copy with these settings applied
    SettingsHolder withSettings(UnaryOperator<PredicateSettings> configurator);

    SettingsHolder withPriority(int priority);

    SettingsHolder withMinCount(int minCount);

    SettingsHolder withMaxCount(int maxCount);

    SettingsHolder withMinSliceCount(int minSliceCount);

    SettingsHolder withMaxSliceCount(int maxSliceCount);

    SettingsHolder withPreviewCount(int previewCount);

    SettingsHolder withDisableRenderFormed(boolean disableRenderFormed);

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
