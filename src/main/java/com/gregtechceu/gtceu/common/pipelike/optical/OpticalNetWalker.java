package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

public class OpticalNetWalker extends PipeNetWalker<OpticalPipeBlockEntity, OpticalPipeProperties, OpticalPipeNet> {

    public static final OpticalRoutePath FAILED_MARKER = new OpticalRoutePath(null, null, 0);

    @Nullable
    public static OpticalRoutePath createNetData(OpticalPipeNet world, BlockPos sourcePipe,
                                                 Direction faceToSourceHandler) {
        OpticalNetWalker walker = new OpticalNetWalker(world, sourcePipe, 1);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? FAILED_MARKER : walker.routePath;
    }

    private OpticalRoutePath routePath;
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    protected OpticalNetWalker(OpticalPipeNet world, BlockPos sourcePipe, int distance) {
        super(world, sourcePipe, distance);
    }

    @Override
    protected PipeNetWalker<OpticalPipeBlockEntity, OpticalPipeProperties, OpticalPipeNet> createSubWalker(OpticalPipeNet world,
                                                                                                           Direction facingToNextPos,
                                                                                                           BlockPos nextPos,
                                                                                                           int walkedBlocks) {
        OpticalNetWalker walker = new OpticalNetWalker(world, nextPos, walkedBlocks);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected void checkPipe(OpticalPipeBlockEntity pipeTile, BlockPos pos) {}

    @Override
    protected void checkNeighbour(OpticalPipeBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {
        if (neighbourTile == null ||
                (pipePos.equals(sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }

        if (((OpticalNetWalker) root).routePath == null) {
            if (neighbourTile.getCapability(GTCapability.CAPABILITY_DATA_ACCESS,
                    faceToNeighbour.getOpposite()).isPresent() ||
                    neighbourTile.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER,
                            faceToNeighbour.getOpposite()).isPresent()) {
                ((OpticalNetWalker) root).routePath = new OpticalRoutePath(pipeTile, faceToNeighbour,
                        getWalkedBlocks());
                stop();
            }
        }
    }

    @Override
    protected Class<OpticalPipeBlockEntity> getBasePipeClass() {
        return OpticalPipeBlockEntity.class;
    }
}
