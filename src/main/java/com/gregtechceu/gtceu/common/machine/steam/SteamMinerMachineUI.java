package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;

final class SteamMinerMachineUI {

    private SteamMinerMachineUI() {}

    static ModularUI create(SteamMinerMachine machine, Player entityPlayer) {
        int rowSize = (int) Math.sqrt(machine.getInventorySize());

        ModularUI builder = new ModularUI(175, 176, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(machine.isHighPressure()));
        builder.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                GuiTextures.SLOT_STEAM.get(machine.isHighPressure()), 7,
                94, true));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(machine.exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18,
                        true, false)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(machine.isHighPressure())));
            }
        }

        builder.widget(new LabelWidget(5, 5, machine.getBlockState().getBlock().getDescriptionId()));
        builder.widget(new PredicatedImageWidget(79, 42, 18, 18,
                GuiTextures.INDICATOR_NO_STEAM.get(machine.isHighPressure()))
                .setPredicate(() -> !machine.drainInput(true)));
        builder.widget(new ImageWidget(7, 16, 105, 75, GuiTextures.DISPLAY_STEAM.get(machine.isHighPressure())));
        builder.widget(new ComponentPanelWidget(10, 19, machine::addDisplayText)
                .setMaxWidthLimit(84));
        builder.widget(new ComponentPanelWidget(70, 19, machine::addDisplayText2)
                .setMaxWidthLimit(84));

        return builder;
    }
}
