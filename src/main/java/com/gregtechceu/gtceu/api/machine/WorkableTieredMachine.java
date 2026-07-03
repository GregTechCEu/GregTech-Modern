package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class WorkableTieredMachine extends TieredEnergyMachine implements IWorkLogicMachine, IMachineLife {

    @Getter
    @Persisted
    @DescSynced
    protected final WorkLogic workLogic;

    public WorkableTieredMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.workLogic = createWorkLogic(args);
    }

    protected WorkLogic createWorkLogic(Object... args) {
        return new WorkLogic(this);
    }

    @Override
    public void serverRunningTick() {}
}
