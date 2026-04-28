package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.VoidingMode;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class AdvancedFluidVoidingCoverUI {

    private AdvancedFluidVoidingCoverUI() {}

    static void buildAdditionalUI(AdvancedFluidVoidingCover cover, WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 20, 20, 20, VoidingMode.values(), cover.getVoidingMode(),
                        cover::setVoidingMode,
                        VoidingMode::getTooltip, CoverModeTextures::getVoidingModeIcon));

        cover.stackSizeInput = new IntInputWidget(35, 20, 84, 20,
                cover::getCurrentBucketModeTransferSize, cover::setCurrentBucketModeTransferSize).setMin(1)
                .setMax(Integer.MAX_VALUE);
        cover.configureStackSizeInput();
        group.addWidget((Widget) cover.stackSizeInput);

        cover.stackSizeBucketModeInput = new EnumSelectorWidget<>(121, 20, 20, 20, BucketMode.values(),
                cover.getTransferBucketMode(), cover::setTransferBucketMode, BucketMode::getTooltip,
                CoverModeTextures::getBucketModeIcon);
        group.addWidget((Widget) cover.stackSizeBucketModeInput);
    }

    static void configureStackSizeInput(AdvancedFluidVoidingCover cover) {
        if (!(cover.stackSizeInput instanceof Widget stackSizeWidget) ||
                !(cover.stackSizeBucketModeInput instanceof Widget stackSizeBucketModeWidget))
            return;

        stackSizeWidget.setVisible(cover.shouldShowStackSize());
        stackSizeBucketModeWidget.setVisible(cover.shouldShowStackSize());
    }

    @SuppressWarnings("unchecked")
    static void setNumberInputValue(Object input, int value) {
        if (input instanceof NumberInputWidget<?>) {
            ((NumberInputWidget<Integer>) input).setValue(value);
        }
    }
}
