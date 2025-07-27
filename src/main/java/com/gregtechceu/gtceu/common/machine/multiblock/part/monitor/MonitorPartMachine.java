package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public class MonitorPartMachine extends MonitorComponentPartMachine {

    public MonitorPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isMonitor() {
        return true;
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return ResourceTexture.fromSpirit(GTCEu.id("item/computer_monitor_cover"));
    }
}
