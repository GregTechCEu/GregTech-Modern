package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IDataAccessMachine;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.computation.ComputationPort;
import com.gregtechceu.gtceu.api.pipenet.IRoutePath;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpticalRoutePath implements IRoutePath<IDataAccessMachine> {

    @Getter
    private final OpticalPipeBlockEntity targetPipe;
    @Getter
    private final Direction targetFacing;
    @Getter
    private final int distance;

    public OpticalRoutePath(OpticalPipeBlockEntity targetPipe, Direction targetFacing, int distance) {
        this.targetPipe = targetPipe;
        this.targetFacing = targetFacing;
        this.distance = distance;
    }

    @Nullable
    public IOpticalDataAccessHatch getDataHatch() {
        IDataAccessMachine dataAccessHatch = getTargetCapability(GTCapability.CAPABILITY_DATA_ACCESS,
                targetPipe.getPipeLevel());
        return dataAccessHatch instanceof IOpticalDataAccessHatch opticalHatch ? opticalHatch : null;
    }

    @Override
    public @NotNull BlockPos getTargetPipePos() {
        return targetPipe.getPipePos();
    }

    @Nullable
    public ComputationPort getComputationPort(Level world) {
        return GTCapabilityHelper.getComputationPort(world, getTargetPipePos().relative(targetFacing),
                targetFacing.getOpposite());
    }

    @Nullable
    @Override
    public IDataAccessMachine getHandler(Level world) {
        return GTCapabilityHelper.getDataAccess(world, getTargetPipePos().relative(targetFacing),
                targetFacing.getOpposite());
    }
}
