package com.gregtechceu.gtceu.common.machines.multiblock.part;

import com.gregtechceu.gtceu.api.machines.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.multiblock.part.TieredPartMachine;

public class AutoMaintenanceHatchPartMachine extends TieredPartMachine implements IMaintenanceMachine {

    public AutoMaintenanceHatchPartMachine(IMachineBlockEntity blockEntity) {
        super(blockEntity, 3);
    }

    @Override
    public void setTaped(boolean ignored) {
    }

    @Override
    public boolean isTaped() {
        return false;
    }

    @Override
    public boolean isFullAuto() {
        return true;
    }

    @Override
    public byte startProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public byte getMaintenanceProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public void setMaintenanceProblems(byte problems) {

    }

    @Override
    public int getTimeActive() {
        return 0;
    }

    @Override
    public void setTimeActive(int time) {

    }

}
