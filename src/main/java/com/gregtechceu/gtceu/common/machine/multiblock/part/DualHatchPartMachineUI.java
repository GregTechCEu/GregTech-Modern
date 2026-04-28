package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

final class DualHatchPartMachineUI {

    private DualHatchPartMachineUI() {}

    static Object createUIWidget(DualHatchPartMachine machine) {
        int slots = machine.getInventorySize();
        int tanks = (int) Math.sqrt(slots);
        var group = new WidgetGroup(0, 0, 18 * (tanks + 1) + 16, 18 * tanks + 16);
        var container = new WidgetGroup(4, 4, 18 * (tanks + 1) + 8, 18 * tanks + 8);

        int index = 0;
        for (int y = 0; y < tanks; y++) {
            for (int x = 0; x < tanks; x++) {
                container.addWidget(new SlotWidget(
                        machine.getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true,
                        machine.getIo().support(IO.IN))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(machine.getIo() == IO.IN ? IngredientIO.INPUT :
                                machine.getIo() == IO.OUT ? IngredientIO.OUTPUT : IngredientIO.BOTH));
            }
        }

        index = 0;
        for (int y = 0; y < tanks; y++) {
            container.addWidget(new TankWidget(
                    machine.tank.getStorages()[index++], 4 + tanks * 18, 4 + y * 18, true,
                    machine.getIo().support(IO.IN))
                    .setBackground(GuiTextures.FLUID_SLOT));
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
