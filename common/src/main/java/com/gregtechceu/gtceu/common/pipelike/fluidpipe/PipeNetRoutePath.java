package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;

public class PipeNetRoutePath {
    @Getter
    private final BlockPos pipePos;
    private final Direction destFacing;
    @Getter
    private final int distance;
    private final Pair<BlockPos, FluidPipeData>[] path;

    public PipeNetRoutePath(BlockPos pipePos, Direction destFacing, Pair<BlockPos, FluidPipeData>[] path, int distance) {
        this.pipePos = pipePos;
        this.destFacing = destFacing;
        this.path = path;
        this.distance = distance;
    }

    public Pair<BlockPos, FluidPipeData>[] getPath() {
        return path;
    }

    public Direction getFaceToHandler() {
        return destFacing;
    }

    public BlockPos getHandlerPos() {
        return pipePos.relative(destFacing);
    }

    @Nullable
    public IFluidTransfer getHandler(Level world) {
        return FluidTransferHelper.getFluidTransfer(world, getHandlerPos(), destFacing.getOpposite());
    }
}
