package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IOSelectorTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.network.chat.Component;

final class ConveyorCoverUI {

    private ConveyorCoverUI() {}

    static Widget createUIWidget(ConveyorCover cover) {
        final var group = new WidgetGroup(0, 0, 176, 137);
        group.addWidget(
                new LabelWidget(10, 5,
                        Component.translatable(cover.getUITitle(), GTValues.VN[cover.tier]).getString()));

        group.addWidget(new IntInputWidget(10, 20, 156, 20, () -> cover.transferRate, cover::setTransferRate)
                .setMin(1).setMax(cover.maxItemTransferRate));

        final EnumSelectorWidget<DistributionMode> distributionSelector = new EnumSelectorWidget<>(146, 67, 20, 20,
                DistributionMode.values(), cover.distributionMode, cover::setDistributionMode,
                DistributionMode::getTooltip,
                CoverModeTextures::getDistributionModeIcon);

        distributionSelector.setVisible(cover.shouldRespectDistributionMode());
        group.addWidget(distributionSelector);

        cover.ioModeSwitch = new SwitchWidget(10, 45, 20, 20,
                (clickData, value) -> {
                    cover.setIo(value ? IO.IN : IO.OUT);
                    if (cover.ioModeSwitch instanceof Widget switchWidget) {
                        switchWidget.setHoverTooltips(
                                LocalizationUtils.format("cover.conveyor.mode",
                                        LocalizationUtils.format(cover.io.tooltip)));
                    }
                })
                .setTexture(
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON,
                                (IGuiTexture) IOSelectorTextures.getIcon(IO.OUT)),
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON,
                                (IGuiTexture) IOSelectorTextures.getIcon(IO.IN)))
                .setPressed(cover.io == IO.IN)
                .setHoverTooltips(
                        LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(cover.io.tooltip)));
        group.addWidget((Widget) cover.ioModeSwitch);

        if (cover.shouldDisplayDistributionMode()) {
            group.addWidget(new EnumSelectorWidget<>(146, 67, 20, 20,
                    DistributionMode.VALUES, cover.distributionMode, cover::setDistributionMode,
                    DistributionMode::getTooltip,
                    CoverModeTextures::getDistributionModeIcon));
        }

        group.addWidget(new EnumSelectorWidget<>(146, 107, 20, 20,
                ManualIOMode.VALUES, cover.manualIOMode, cover::setManualIOMode, ManualIOMode::getTooltip,
                CoverModeTextures::getManualIOModeIcon)
                .setHoverTooltips("cover.universal.manual_import_export.mode.description"));

        group.addWidget((Widget) cover.filterHandler.createFilterSlotUI(125, 108));
        group.addWidget((Widget) cover.filterHandler.createFilterConfigUI(10, 72, 156, 60));

        if (cover instanceof RobotArmCover robotArmCover) {
            RobotArmCoverUI.buildAdditionalUI(robotArmCover, group);
        }

        return group;
    }
}
