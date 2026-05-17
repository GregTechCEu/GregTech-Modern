package com.gregtechceu.gtceu.api.cover.filter;

import net.minecraft.nbt.CompoundTag;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Filter<T, S extends Filter<T, S>> extends Predicate<T> {

    /**
     * @return Filter panel when opened by itself (including the player inventory)
     */
    ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings);

    CompoundTag saveFilter();

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default boolean isBlank() {
        return false;
    }
}
