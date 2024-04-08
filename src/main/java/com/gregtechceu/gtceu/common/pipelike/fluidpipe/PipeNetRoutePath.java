package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.pipenet.IRoutePath;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import org.jetbrains.annotations.Nullable;

public class PipeNetRoutePath implements IRoutePath<IFluidTransfer> {
    @Getter
    private final BlockPos pipePos;
    @Getter
    private final Direction targetFacing;
    @Getter
    private final int distance;
    private final Pair<BlockPos, FluidPipeData>[] path;

    public PipeNetRoutePath(BlockPos pipePos, Direction targetFacing, Pair<BlockPos, FluidPipeData>[] path, int distance) {
        this.pipePos = pipePos;
        this.targetFacing = targetFacing;
        this.path = path;
        this.distance = distance;
    }

    public Pair<BlockPos, FluidPipeData>[] getPath() {
        return path;
    }

    @Override
    @NotNull
    public BlockPos getTargetPipePos() {
        return pipePos.relative(targetFacing);
    }

    @Nullable
    public IFluidTransfer getHandler(Level world) {
        return FluidTransferHelper.getFluidTransfer(world, getTargetPipePos(), targetFacing.getOpposite());
    }
}
