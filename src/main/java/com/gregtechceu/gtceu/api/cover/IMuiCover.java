package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;

public interface IMuiCover extends IUIHolder<SidedPosGuiData> {

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    default boolean isRemote() {
        return self().coverHolder.isRemote();
    }

    default boolean isRemoved() {
        return self().coverHolder.isRemoved() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.COVER;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    default ModularScreen createScreen(SidedPosGuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }

    @Override
    default ModularPanel<?> buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel<?> panel = ModularPanel.defaultPanel(this.self().coverDefinition.getId().getPath(), 176, 192 + 18);

        panel.child(GTMuiWidgets.createTitleBar(this.self().getAttachItem(), 176, GTGuiTextures.BACKGROUND));

        Flow column = Flow.column()
                .top(7).margin(7, 0)
                .childPadding(2)
                .widthRel(1.0f).coverChildrenHeight();

        createCoverUIRows(column, data, syncManager, settings);
        return panel.child(column)
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    /**
     * The default cover UI panel builds a single column with rows added by each cover.
     */
    default void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                   UISettings settings) {}

    /* Helper methods for UI creation with covers that are commonly used */

    /**
     * The color used for Cover UI text. Available for reference, but is
     * handled automatically by the {@link GTGuiTheme#COVER} theme.
     */
    int UI_TEXT_COLOR = 0xFF555555;

    default Flow coverUIRow() {
        return Flow.row()
                .coverChildrenHeight()
                .widthRel(1f)
                .childPadding(2);
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default <T extends Enum<T>> BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }
}
