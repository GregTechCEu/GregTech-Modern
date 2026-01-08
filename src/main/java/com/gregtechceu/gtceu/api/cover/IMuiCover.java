package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public interface IMuiCover extends IUIHolder<SidedPosGuiData> {

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    default boolean isRemoved() {
        return self().coverHolder.isRemoved() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.COVER;
    }

    @Override
    default ModularPanel buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IWidget widget = createCoverUI(data, syncManager, settings);
        return GTGuis.createPanel(this.self(), 176, 166)
                .background(GTGuiTextures.BACKGROUND)
                .child(widget)
                .bindPlayerInventory();
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

    default IKey createAdjustOverlay(boolean increment) {
        final StringBuilder builder = new StringBuilder();
        builder.append(increment ? '+' : '-');
        builder.append(getIncrementValue(MouseData.create(-1)));

        float scale = 1f;
        if (builder.length() == 3) {
            scale = 0.8f;
        } else if (builder.length() == 4) {
            scale = 0.6f;
        } else if (builder.length() > 4) {
            scale = 0.5f;
        }
        return IKey.str(builder.toString())
                .color(Color.WHITE.main)
                .scale(scale);
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

    class EnumRowBuilder<T extends Enum<T>> {

        private EnumSyncValue<T> syncValue;
        private final Class<T> enumValue;
        private String lang;
        private IDrawable[] background;
        private IDrawable selectedBackground;
        private IDrawable[] overlay;

        public EnumRowBuilder(Class<T> enumValue) {
            this.enumValue = enumValue;
        }

        public EnumRowBuilder<T> value(EnumSyncValue<T> syncValue) {
            this.syncValue = syncValue;
            return this;
        }

        public EnumRowBuilder<T> lang(String lang) {
            this.lang = lang;
            return this;
        }

        public EnumRowBuilder<T> background(IDrawable... background) {
            this.background = background;
            return this;
        }

        public EnumRowBuilder<T> selectedBackground(IDrawable selectedBackground) {
            this.selectedBackground = selectedBackground;
            return this;
        }

        public EnumRowBuilder<T> overlay(IDrawable... overlay) {
            this.overlay = overlay;
            return this;
        }

        public EnumRowBuilder<T> overlay(int size, IDrawable... overlay) {
            this.overlay = new IDrawable[overlay.length];
            for (int i = 0; i < overlay.length; i++) {
                this.overlay[i] = overlay[i].asIcon().size(size);
            }
            return this;
        }

        private BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
            return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
        }

        public Flow build() {
            var row = Flow.row().marginBottom(2).coverChildrenHeight().widthRel(1f);
            if (this.enumValue != null && this.syncValue != null) {
                for (var enumVal : enumValue.getEnumConstants()) {
                    var button = new ToggleButton().size(18).marginRight(2)
                            .value(boolValueOf(this.syncValue, enumVal));

                    if (this.background != null && this.background.length > 0)
                        button.background(this.background);
                    else
                        button.background(GTGuiTextures.MC_BUTTON);

                    if (this.selectedBackground != null)
                        button.selectedBackground(this.selectedBackground);
                    else
                        button.selectedBackground(GTGuiTextures.MC_BUTTON_DISABLED);

                    if (this.overlay != null)
                        button.overlay(this.overlay[enumVal.ordinal()]);

                    if (enumVal instanceof StringRepresentable serializable) {
                        button.addTooltipLine(IKey.lang(serializable.getSerializedName()));
                    }
                    row.child(button);
                }
            }

            if (this.lang != null && !this.lang.isEmpty())
                row.child(IKey.lang(this.lang).asWidget().align(Alignment.CenterRight).height(18));

            return row;
        }
    }
}
