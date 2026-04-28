package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class RobotArmCoverUI {

    private RobotArmCoverUI() {}

    static void buildAdditionalUI(RobotArmCover cover, WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), cover.transferMode,
                        cover::setTransferMode,
                        TransferMode::getTooltip, CoverModeTextures::getTransferModeIcon));

        cover.stackSizeInput = new IntInputWidget(64, 45, 80, 20,
                () -> cover.globalTransferLimit, val -> cover.globalTransferLimit = val);
        configureStackSizeInput(cover);

        group.addWidget((IntInputWidget) cover.stackSizeInput);
    }

    static void configureStackSizeInput(RobotArmCover cover) {
        if (!(cover.stackSizeInput instanceof IntInputWidget input))
            return;

        input.setVisible(cover.shouldShowStackSize());
        input.setMin(1);
        input.setMax(cover.transferMode.maxStackSize);
    }
}
