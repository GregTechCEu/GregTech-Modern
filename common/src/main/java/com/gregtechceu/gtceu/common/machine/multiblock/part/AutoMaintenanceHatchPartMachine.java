package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IMaintenanceHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import net.minecraft.util.Tuple;

public class AutoMaintenanceHatchPartMachine extends TieredPartMachine implements IMaintenanceHatch {

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
    public void storeMaintenanceData(byte ignored1, int ignored2) {
    }

    @Override
    public boolean hasMaintenanceData() {
        return true;
    }

    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        return new Tuple<>((byte) 0b111111, 0);
    }

    @Override
    public boolean isFullAuto() {
        return true;
    }

    @Override
    public double getDurationMultiplier() {
        return 1.0;
    }

    @Override
    public double getTimeMultiplier() {
        return 1.0;
    }

    @Override
    public boolean startWithoutProblems() {
        return true;
    }

    @Override
    public boolean canShared() {
        return false;
    }
}
