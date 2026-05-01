package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class BufferMachineUI {

    private BufferMachineUI() {}

    static Widget createUIWidget(BufferMachine machine) {
        int invTier = BufferMachine.getTankSize(machine.getTier());
        var group = new WidgetGroup(0, 0, 18 * (invTier + 1) + 16, 18 * invTier + 16);
        var container = new WidgetGroup(4, 4, 18 * (invTier + 1) + 8, 18 * invTier + 8);

        int index = 0;
        for (int y = 0; y < invTier; y++) {
            for (int x = 0; x < invTier; x++) {
                container.addWidget(new SlotWidget(
                        machine.getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        index = 0;
        for (int y = 0; y < invTier; y++) {
            container.addWidget(new TankWidget(
                    machine.getTank().getStorages()[index++], 4 + invTier * 18, 4 + y * 18, true, true)
                    .setBackground(GuiTextures.FLUID_SLOT));
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
