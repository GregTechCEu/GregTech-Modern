package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import java.util.List;

final class AdvancedItemDetectorCoverUI {

    private AdvancedItemDetectorCoverUI() {}

    static Widget createUIWidget(AdvancedItemDetectorCover cover) {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 170);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_item_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 65,
                List.of(LocalizationUtils.format("cover.advanced_item_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 65,
                List.of(LocalizationUtils.format("cover.advanced_item_detector.max"))));

        group.addWidget(new IntInputWidget(80, 50, 176 - 80 - 10, 20, cover::getMinValue, cover::setMinValue));
        group.addWidget(new IntInputWidget(80, 75, 176 - 80 - 10, 20, cover::getMaxValue, cover::setMaxValue));

        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, cover::isInverted, cover::setInverted)
                .isMultiLang()
                .setTooltipText("cover.advanced_item_detector.invert"));

        group.addWidget(new ToggleButtonWidget(31, 21, 18, 18,
                GuiTextures.BUTTON_LOCK, cover::isLatched, cover::setLatched)
                .setShouldUseBaseBackground()
                .isMultiLang()
                .setTooltipText("cover.advanced_detector.latch"));

        group.addWidget((Widget) cover.filterHandler.createFilterSlotUI(148, 100));
        group.addWidget((Widget) cover.filterHandler.createFilterConfigUI(10, 100, 156, 60));

        return group;
    }
}
