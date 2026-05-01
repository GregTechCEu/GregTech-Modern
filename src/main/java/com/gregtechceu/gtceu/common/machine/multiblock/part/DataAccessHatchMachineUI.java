package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class DataAccessHatchMachineUI {

    private DataAccessHatchMachineUI() {}

    static Widget createUIWidget(DataAccessHatchMachine machine) {
        int rowSize = (int) Math.sqrt(machine.getInventorySize());
        int xOffset = 18 * rowSize / 2;
        WidgetGroup group = new WidgetGroup(0, 0, 18 * rowSize, 18 * rowSize);

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                group.addWidget(new SlotWidget(machine.importItems, index,
                        rowSize * 9 + x * 18 - xOffset, y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return group;
    }
}
