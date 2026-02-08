package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.world.item.ItemStack;

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

        return panel.child(createCoverUI(data, syncManager, settings))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    ParentWidget<?> createCoverUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings);

    /* Helper methods for UI creation with covers that are commonly used */

    /**
     * The color used for Cover UI titles, and used in {@link #createTitleRow}.
     */
    int UI_TITLE_COLOR = 0xFF222222;
    /**
     * The color used for Cover UI text. Available for reference, but is
     * handled automatically by the {@link GTGuiTheme#COVER} theme.
     */
    int UI_TEXT_COLOR = 0xFF555555;

    /**
     * Create the Title bar widget for a Cover.
     */
    static Flow createTitleRow(ItemStack stack) {
        return Flow.row()
                .height(16).coverChildrenWidth()
                .child(new ItemDrawable(stack).asWidget().size(16).marginRight(4))
                .child(IKey.lang(stack.getHoverName())
                        .color(UI_TITLE_COLOR)
                        .asWidget().heightRel(1.0f));
    }

    /**
     * Create a new settings row for a Cover setting.
     */
    default ParentWidget<?> createSettingsRow() {
        return new ParentWidget<>().height(16).widthRel(1.0f).marginBottom(2);
    }

    default int getIncrementValue() {
        return getIncrementValue(MouseData.create(-1));
    }

    default int getIncrementValue(MouseData data) {
        int adjust = 1;
        if (data.shift()) adjust *= 4;
        if (data.ctrl()) adjust *= 16;
        if (data.alt()) adjust *= 64;
        return adjust;
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default <T extends Enum<T>> BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default BoolValue.Dynamic boolValueOf(IntSyncValue syncValue, int value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }
}
