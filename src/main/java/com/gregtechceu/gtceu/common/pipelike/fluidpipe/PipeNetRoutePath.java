package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

public class PipeNetRoutePath implements IRoutePath<IFluidHandler> {

    @Getter
    private final BlockPos pipePos;
    @Getter
    private final Direction targetFacing;
    @Getter
    private final int distance;
    private final Pair<BlockPos, FluidPipeData>[] path;

    public PipeNetRoutePath(BlockPos pipePos, Direction targetFacing, Pair<BlockPos, FluidPipeData>[] path,
                            int distance) {
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
    public IFluidHandler getHandler(Level world) {
        return world.getCapability(Capabilities.FluidHandler.BLOCK, getTargetPipePos(), targetFacing.getOpposite());
    }
}
