package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IOSelectorTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

final class PumpCoverUI {

    private PumpCoverUI() {}

    static Object createUIWidget(PumpCover cover) {
        final var group = new WidgetGroup(0, 0, 176, 137);
        group.addWidget(
                new LabelWidget(10, 5,
                        Component.translatable(cover.getUITitle(), GTValues.VN[cover.tier]).getString()));

        cover.transferRateWidget = new IntInputWidget(10, 20, 134, 20,
                cover::getCurrentBucketModeTransferRate, cover::setCurrentBucketModeTransferRate).setMin(0);
        configureTransferRateWidget(cover, cover.bucketMode.multiplier, cover.bucketMode.multiplier);
        group.addWidget((Widget) cover.transferRateWidget);

        group.addWidget(new EnumSelectorWidget<>(
                146, 20, 20, 20,
                Arrays.stream(BucketMode.values()).filter(m -> m.multiplier <= cover.maxFluidTransferRate).toList(),
                cover.bucketMode, cover::setBucketMode, BucketMode::getTooltip,
                CoverModeTextures::getBucketModeIcon).setTooltipSupplier(PumpCoverUI::getBucketModeTooltip));

        group.addWidget(new EnumSelectorWidget<>(10, 45, 20, 20, List.of(IO.IN, IO.OUT), cover.io, cover::setIo,
                IO::getTooltip, IOSelectorTextures::getIcon));

        group.addWidget(new EnumSelectorWidget<>(146, 107, 20, 20,
                ManualIOMode.VALUES, cover.manualIOMode, cover::setManualIOMode, ManualIOMode::getTooltip,
                CoverModeTextures::getManualIOModeIcon)
                .setHoverTooltips("cover.universal.manual_import_export.mode.description"));

        group.addWidget((Widget) cover.filterHandler.createFilterSlotUI(125, 108));
        group.addWidget((Widget) cover.filterHandler.createFilterConfigUI(10, 72, 156, 60));

        if (cover instanceof FluidRegulatorCover fluidRegulatorCover) {
            FluidRegulatorCoverUI.buildAdditionalUI(fluidRegulatorCover, group);
        }

        return group;
    }

    static void configureTransferRateWidget(PumpCover cover, int oldMultiplier, int newMultiplier) {
        if (!(cover.transferRateWidget instanceof NumberInputWidget<?> input)) return;

        if (oldMultiplier > newMultiplier) {
            setNumberInputValue(input, cover.getCurrentBucketModeTransferRate());
        }

        setNumberInputMax(input, cover.maxFluidTransferRate / cover.bucketMode.multiplier);

        if (newMultiplier > oldMultiplier) {
            setNumberInputValue(input, cover.getCurrentBucketModeTransferRate());
        }
    }

    private static List<Component> getBucketModeTooltip(BucketMode mode, String langKey) {
        return List.of(
                Component.translatable(langKey).append(Component.translatable("gtceu.gui.content.units.per_tick")));
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
