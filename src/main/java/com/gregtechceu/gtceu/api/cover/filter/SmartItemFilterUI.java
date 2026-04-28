package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import org.jetbrains.annotations.NotNull;

final class SmartItemFilterUI {

    private SmartItemFilterUI() {}

    static Object openConfigurator(SmartItemFilter filter, int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3);
        group.addWidget(new EnumSelectorWidget<>(16, 8, 32, 32,
                ModeSelection.values(), ModeSelection.of(filter.filterMode),
                selection -> filter.setFilterMode(selection.mode)));
        return group;
    }

    static Object icon(String name) {
        return new ResourceTexture("gtceu:textures/block/machines/" + name + "/overlay_front.png");
    }

    private enum ModeSelection implements EnumSelectorWidget.SelectableEnum {

        ELECTROLYZER(SmartItemFilter.SmartFilteringMode.ELECTROLYZER),
        CENTRIFUGE(SmartItemFilter.SmartFilteringMode.CENTRIFUGE),
        SIFTER(SmartItemFilter.SmartFilteringMode.SIFTER);

        private final SmartItemFilter.SmartFilteringMode mode;

        ModeSelection(SmartItemFilter.SmartFilteringMode mode) {
            this.mode = mode;
        }

        private static ModeSelection of(SmartItemFilter.SmartFilteringMode mode) {
            return switch (mode) {
                case ELECTROLYZER -> ELECTROLYZER;
                case CENTRIFUGE -> CENTRIFUGE;
                case SIFTER -> SIFTER;
            };
        }

        @Override
        public @NotNull String getTooltip() {
            return mode.getTooltip();
        }

        @Override
        public @NotNull Object getIcon() {
            return mode.getIcon();
        }
    }
}
