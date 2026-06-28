package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Direction;

public class MaintenanceDetectorCover extends DetectorCover {

    public MaintenanceDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        if (!ConfigHolder.INSTANCE.machines.enableMaintenance) {
            return false;
        }

        return super.canAttach() && coverHolder instanceof MaintenanceHatchPartMachine;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0) {
            return;
        }

        MaintenanceHatchPartMachine maintenance = (MaintenanceHatchPartMachine)coverHolder;

        int signal = getRedstoneSignalOutput();
        boolean shouldSignal = isInverted() != maintenance.hasMaintenanceProblems();

        if (shouldSignal && signal != 15) {
            setRedstoneSignalOutput(15);
        } else if (!shouldSignal && signal == 15) {
            setRedstoneSignalOutput(0);
        }
    }
}
