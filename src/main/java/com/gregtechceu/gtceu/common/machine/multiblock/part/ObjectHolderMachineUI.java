package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

final class ObjectHolderMachineUI {

    private ObjectHolderMachineUI() {}

    static Object createUIWidget(ObjectHolderMachine machine) {
        return new WidgetGroup(new Position(0, 0))
                .addWidget(new ImageWidget(46, 15, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .addWidget(new BlockableSlotWidget(machine.heldItems, 0, 79, 36)
                        .setIsBlocked(machine::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.RESEARCH_STATION_OVERLAY))
                .addWidget(new BlockableSlotWidget(machine.heldItems, 1, 15, 36)
                        .setIsBlocked(machine::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY));
    }
}
