package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class MaintenanceDetectorCover extends DetectorCover {

    public MaintenanceDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_maintenance_detector"), null).build();
    }

    @Override
    public boolean canAttach(@NotNull ICoverable coverable, @NotNull Direction side) {
        if (!ConfigHolder.INSTANCE.machines.enableMaintenance) {
            return false;
        }

        return coverable.getCapability(GTCapability.CAPABILITY_MAINTENANCE_MACHINE).isPresent();
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0) {
            return;
        }

        IMaintenanceMachine maintenance = GTCapabilityHelper.getMaintenanceMachine(coverHolder.getLevel(),
                coverHolder.getPos(), attachedSide);

        int signal = getRedstoneSignalOutput();
        boolean shouldSignal = isInverted() != maintenance.hasMaintenanceProblems();

        if (shouldSignal && signal != 15) {
            setRedstoneSignalOutput(15);
        } else if (!shouldSignal && signal == 15) {
            setRedstoneSignalOutput(0);
        }
    }
}
