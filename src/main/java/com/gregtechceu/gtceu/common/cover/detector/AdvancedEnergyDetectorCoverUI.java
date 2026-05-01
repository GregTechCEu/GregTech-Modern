package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import java.util.List;

final class AdvancedEnergyDetectorCoverUI {

    private AdvancedEnergyDetectorCoverUI() {}

    static Widget createUIWidget(AdvancedEnergyDetectorCover cover) {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 105);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_energy_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.max"))));

        cover.minValueInput = new LongInputWidget(40, 50, 176 - 40 - 10, 20, cover::getMinValue, cover::setMinValue);
        cover.maxValueInput = new LongInputWidget(40, 75, 176 - 40 - 10, 20, cover::getMaxValue, cover::setMaxValue);
        initializeMinMaxInputs(cover, cover.isUsePercent());
        group.addWidget((LongInputWidget) cover.minValueInput);
        group.addWidget((LongInputWidget) cover.maxValueInput);

        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, cover::isInverted, cover::setInverted)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.invert"));

        group.addWidget(new ToggleButtonWidget(
                176 - 29, 20, 20, 20,
                GuiTextures.ENERGY_DETECTOR_COVER_MODE_BUTTON, cover::isUsePercent, cover::setUsePercent)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.use_percent"));

        return group;
    }

    static void initializeMinMaxInputs(AdvancedEnergyDetectorCover cover, boolean wasPercent) {
        if (GTCEu.isClientThread() || !(cover.minValueInput instanceof LongInputWidget minInput) ||
                !(cover.maxValueInput instanceof LongInputWidget maxInput))
            return;

        long energyCapacity;
        try {
            energyCapacity = cover.getEnergyInfoProvider().getEnergyInfo().capacity().longValueExact();
        } catch (ArithmeticException e) {
            energyCapacity = Long.MAX_VALUE;
        }

        minInput.setMin(0L);
        maxInput.setMin(0L);

        if (cover.isUsePercent()) {
            if (!wasPercent) {
                minInput.setValue(GTMath.clamp((long) (((double) cover.minValue / energyCapacity) * 100), 0, 100));
                maxInput.setValue(GTMath.clamp((long) (((double) cover.maxValue / energyCapacity) * 100), 0, 100));
            }

            minInput.setMax(100L);
            maxInput.setMax(100L);
        } else {
            minInput.setMax(energyCapacity);
            maxInput.setMax(energyCapacity);

            if (wasPercent) {
                minInput.setValue(
                        GTMath.clamp((long) Math.ceil((cover.minValue / 100.0) * energyCapacity), 0, energyCapacity));
                maxInput.setValue(
                        GTMath.clamp((long) Math.ceil((cover.maxValue / 100.0) * energyCapacity), 0, energyCapacity));
            }
        }
    }
}
