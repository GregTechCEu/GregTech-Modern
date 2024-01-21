package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.pipenet.IRoutePath;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EnergyRoutePath implements IRoutePath<IEnergyContainer> {
    private final CableBlockEntity targetPipe;
    @Getter
    private final BlockPos targetPipePos;
    @Getter
    private final Direction targetFacing;
    @Getter
    private final int distance;
    @Getter
    private final CableBlockEntity[] path;
    @Getter
    private final long maxLoss;

    public EnergyRoutePath(BlockPos targetPipePos, Direction targetFacing, CableBlockEntity[] path, int distance, long maxLoss) {
        this.targetPipe = path[path.length - 1];
        this.targetPipePos = targetPipePos;
        this.targetFacing = targetFacing;
        this.path = path;
        this.distance = distance;
        this.maxLoss = maxLoss;
    }

    public BlockPos getHandlerPos() {
        return targetPipePos.relative(targetFacing);
    }

    @Nullable
    public IEnergyContainer getHandler(Level world) {
        return GTCapabilityHelper.getEnergyContainer(world, getHandlerPos(), targetFacing.getOpposite());
    }
}
