package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

final class FusionReactorMachineUI {

    private FusionReactorMachineUI() {}

    static void addEUToStartLabel(Object groupObject, long euToStart, String fusionName) {
        if (!(groupObject instanceof WidgetGroup group)) {
            return;
        }
        group.addWidget(new LabelWidget(-8, group.getSizeHeight() - 10,
                LocalizationUtils.format("gtceu.recipe.eu_to_start",
                        FormattingUtil.formatNumberReadable2F(euToStart, false),
                        fusionName)));
    }
}
