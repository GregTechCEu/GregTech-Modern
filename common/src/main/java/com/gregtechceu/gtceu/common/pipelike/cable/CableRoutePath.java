package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;

public class CableRoutePath {
    private final BlockPos destPipePos;
    private final Direction destFacing;
    private final int distance;
    private final Pair<BlockPos, CableData>[] path;
    private final long maxLoss;

    public CableRoutePath(BlockPos destPipePos, Direction destFacing, Pair<BlockPos, CableData>[] path, int distance, long maxLoss) {
        this.destPipePos = destPipePos;
        this.destFacing = destFacing;
        this.path = path;
        this.distance = distance;
        this.maxLoss = maxLoss;
    }

    public int getDistance() {
        return distance;
    }

    public long getMaxLoss() {
        return maxLoss;
    }

    public Pair<BlockPos, CableData>[] getPath() {
        return path;
    }

    public BlockPos getPipePos() {
        return destPipePos;
    }

    public Direction getFaceToHandler() {
        return destFacing;
    }

    public BlockPos getHandlerPos() {
        return destPipePos.relative(destFacing);
    }

    @Nullable
    public IEnergyContainer getHandler(Level world) {
        return GTCapabilityHelper.getEnergyContainer(world, getHandlerPos(), destFacing.getOpposite());
    }
}
