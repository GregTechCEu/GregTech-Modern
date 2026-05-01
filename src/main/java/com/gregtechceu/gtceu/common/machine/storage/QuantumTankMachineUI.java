package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class QuantumTankMachineUI {

    private QuantumTankMachineUI() {}

    static Widget createUIWidget(QuantumTankMachine machine) {
        var group = new WidgetGroup(0, 0, 90, 63);
        group.addWidget(new ImageWidget(4, 4, 82, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () -> FormattingUtil.formatBuckets(machine.storedAmount))
                        .setTextColor(-1)
                        .setDropShadow(false))
                .addWidget(new TankWidget(machine.cache, 0, 68, 23, true, true)
                        .setShowAmount(false)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .addWidget(new PhantomFluidWidget(machine.lockedFluid, 0, 68, 41, 18, 18,
                        machine::getLockedFluid, machine::setLocked)
                        .setShowAmount(false)
                        .setBackground(ColorPattern.T_GRAY.rectTexture()))
                .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, machine.autoOutput::isAutoOutputFluids,
                        machine.autoOutput::setAllowAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
                        GuiTextures.BUTTON_LOCK, machine::isLocked, machine::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_lock.tooltip"))
                .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
                        GuiTextures.BUTTON_VOID, () -> machine.isVoiding, b -> machine.isVoiding = b)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_voiding_partial.tooltip"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }
}
