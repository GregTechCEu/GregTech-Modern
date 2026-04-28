package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachineUI;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;

final class SteamParallelMultiblockMachineUI {

    private SteamParallelMultiblockMachineUI() {}

    static ModularUI createUI(SteamParallelMultiblockMachine machine, Player entityPlayer) {
        boolean steel = ConfigHolder.INSTANCE.machines.steelSteamMultiblocks;
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121)
                .setBackground(IDisplayUIMachineUI.screenTexture(machine.getScreenTexture()));
        screen.addWidget(new LabelWidget(4, 5, machine.self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, machine::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(machine::handleDisplayClick));
        return new ModularUI(176, 216, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(steel))
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(steel), 7, 134, true));
    }
}
