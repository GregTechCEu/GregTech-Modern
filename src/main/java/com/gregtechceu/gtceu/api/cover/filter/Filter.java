package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Filter<T, S extends Filter<T, S>> extends Predicate<T> {

    /**
     * @return Filter panel when opened by itself (including the player inventory)
     */
    default ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings,
                                     boolean showPlayerInventory) {
        return new Dialog<>(getFilterItem().getItem().toString())
                .disablePanelsBelow(false)
                .draggable(true)
                .coverChildrenHeight()
                .child(GTMuiWidgets.createTitleBar(this::getFilterItem, 176, GTGuiTextures.BACKGROUND))
                .child(Flow.col().coverChildrenHeight()
                        .child(getFilterUI(data, syncManager, settings).marginTop(10).marginBottom(10))
                        .childIf(showPlayerInventory,
                                () -> SlotGroupWidget.playerInventory(false).marginLeft(7).marginBottom(7)));
    }

    ItemStack getFilterItem();

    Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings);

    CompoundTag saveFilter();

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default boolean isBlank() {
        return false;
    }
}
