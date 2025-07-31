package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

public abstract class MonitorComponentPartMachine extends MultiblockPartMachine implements IMonitorComponent {

    public MonitorComponentPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }
}
