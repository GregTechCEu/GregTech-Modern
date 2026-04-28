package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;

final class PumpMachineUI {

    private PumpMachineUI() {}

    static ModularUI create(PumpMachine machine, Player entityPlayer) {
        return new ModularUI(176, 166, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> machine.cache.getFluidInTank(0).getAmount() + "")
                        .setTextColor(-1)
                        .setDropShadow(true))
                .widget(new LabelWidget(6, 6, machine.getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(machine.cache.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(new ToggleButtonWidget(7, 53, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, machine.autoOutput::isAutoOutputFluids,
                        machine.autoOutput::setAllowAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }
}
