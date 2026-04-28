package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class FluidRegulatorCoverUI {

    private FluidRegulatorCoverUI() {}

    static void buildAdditionalUI(FluidRegulatorCover cover, WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), cover.getTransferMode(),
                        cover::setTransferMode,
                        TransferMode::getTooltip, CoverModeTextures::getTransferModeIcon));

        cover.transferSizeInput = new IntInputWidget(35, 45, 84, 20,
                cover::getCurrentBucketModeTransferSize, cover::setCurrentBucketModeTransferSize).setMin(0)
                .setMax(Integer.MAX_VALUE);
        cover.configureTransferSizeInput();
        group.addWidget((Widget) cover.transferSizeInput);

        cover.transferBucketModeInput = new EnumSelectorWidget<>(121, 45, 20, 20, BucketMode.values(),
                cover.getTransferBucketMode(), cover::setTransferBucketMode, BucketMode::getTooltip,
                CoverModeTextures::getBucketModeIcon);
        group.addWidget((Widget) cover.transferBucketModeInput);
    }

    static void configureTransferSizeInputVisibility(FluidRegulatorCover cover) {
        if (!(cover.transferSizeInput instanceof Widget transferSizeWidget) ||
                !(cover.transferBucketModeInput instanceof Widget transferBucketModeWidget))
            return;

        transferSizeWidget.setVisible(cover.shouldShowTransferSize());
        transferBucketModeWidget.setVisible(cover.shouldShowTransferSize());
    }

    static void configureTransferSizeInputValue(FluidRegulatorCover cover, int oldMultiplier, int newMultiplier) {
        if (!(cover.transferSizeInput instanceof NumberInputWidget<?> input)) return;

        if (oldMultiplier > newMultiplier) {
            setNumberInputValue(input, cover.getCurrentBucketModeTransferSize());
        }
        setNumberInputMax(input, FluidRegulatorCover.MAX_STACK_SIZE / cover.getTransferBucketMode().multiplier);
        if (newMultiplier > oldMultiplier) {
            setNumberInputValue(input, cover.getCurrentBucketModeTransferSize());
        }
    }

    @SuppressWarnings("unchecked")
    private static void setNumberInputValue(Object input, int value) {
        ((NumberInputWidget<Integer>) input).setValue(value);
    }

    @SuppressWarnings("unchecked")
    private static void setNumberInputMax(Object input, int value) {
        ((NumberInputWidget<Integer>) input).setMax(value);
    }
}
