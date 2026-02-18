package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTGuis;

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
    default ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = GTGuis.createPanel(this.self(), 176, 192 + 18);

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
