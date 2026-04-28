package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;

final class SteamHatchPartMachineUI {

    private SteamHatchPartMachineUI() {}

    static ModularUI create(SteamHatchPartMachine machine, Player entityPlayer) {
        return new ModularUI(176, 166, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(SteamHatchPartMachine.IS_STEEL))
                .widget(new ImageWidget(7, 16, 81, 55,
                        GuiTextures.DISPLAY_STEAM.get(SteamHatchPartMachine.IS_STEEL)))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> machine.tank.getFluidInTank(0).getAmount() + "")
                        .setTextColor(-1)
                        .setDropShadow(true))
                .widget(new LabelWidget(6, 6, machine.getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(machine.tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(SteamHatchPartMachine.IS_STEEL), 7, 84, true));
    }
}
