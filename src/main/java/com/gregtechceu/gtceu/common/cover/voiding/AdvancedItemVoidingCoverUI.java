package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.VoidingMode;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class AdvancedItemVoidingCoverUI {

    private AdvancedItemVoidingCoverUI() {}

    static void buildAdditionalUI(AdvancedItemVoidingCover cover, WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 20, 20, 20, VoidingMode.values(), cover.getVoidingMode(),
                        cover::setVoidingMode,
                        VoidingMode::getTooltip, CoverModeTextures::getVoidingModeIcon));

        cover.stackSizeInput = new IntInputWidget(64, 20, 80, 20,
                () -> cover.globalVoidingLimit, val -> cover.globalVoidingLimit = val);
        configureStackSizeInput(cover);

        group.addWidget((IntInputWidget) cover.stackSizeInput);
    }

    static void configureStackSizeInput(AdvancedItemVoidingCover cover) {
        if (!(cover.stackSizeInput instanceof IntInputWidget input))
            return;

        input.setVisible(cover.shouldShowStackSize());
        input.setMin(1);
        input.setMax(cover.getVoidingMode().maxStackSize);
    }
}
