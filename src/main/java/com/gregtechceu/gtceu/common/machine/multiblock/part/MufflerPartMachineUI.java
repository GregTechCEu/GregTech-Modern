package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;

final class MufflerPartMachineUI {

    private MufflerPartMachineUI() {}

    static ModularUI create(MufflerPartMachine machine, Player entityPlayer) {
        int rowSize = (int) Math.sqrt(machine.getInventory().getSlots());
        int xOffset = rowSize == 10 ? 9 : 0;
        var modular = new ModularUI(176 + xOffset * 2,
                18 + 18 * rowSize + 94, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, machine.getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset,
                        18 + 18 * rowSize + 12, true));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                modular.widget(new SlotWidget(machine.getInventory(), index,
                        (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return modular;
    }
}
