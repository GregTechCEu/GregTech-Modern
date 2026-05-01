package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class FluidVoidingCoverUI {

    private FluidVoidingCoverUI() {}

    static Widget createUIWidget(FluidVoidingCover cover) {
        final var group = new WidgetGroup(0, 0, 176, 120);
        group.addWidget(new LabelWidget(10, 5, cover.getUITitle()));

        group.addWidget(new ToggleButtonWidget(10, 20, 20, 20,
                GuiTextures.BUTTON_POWER, cover::isWorkingEnabled, cover::setWorkingEnabled));

        group.addWidget((Widget) cover.getVoidingFilterHandler().createFilterSlotUI(148, 91));
        group.addWidget((Widget) cover.getVoidingFilterHandler().createFilterConfigUI(10, 50, 126, 60));

        if (cover instanceof AdvancedFluidVoidingCover advancedCover) {
            AdvancedFluidVoidingCoverUI.buildAdditionalUI(advancedCover, group);
        }

        return group;
    }
}
