package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNetWalker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LaserNetWalker extends PipeNetWalker<LaserPipeNet.LaserData, LaserPipeNet> {

    @Nullable
    public static LaserPipeNet.LaserData createNetData(LaserPipeNet world, BlockPos sourcePipe, Direction faceToSourceHandler) {
        LaserNetWalker walker = new LaserNetWalker(world, sourcePipe, 1, null, null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.axis = faceToSourceHandler.getAxis();
        walker.traversePipeNet();
        return walker.isInvalid() ? null : walker.laserData;
    }

    private LaserPipeProperties minProperties;
    private LaserPipeNet.LaserData laserData;
    private BlockPos sourcePipe;
    private Direction facingToHandler;
    private Direction.Axis axis;

    protected LaserNetWalker(LaserPipeNet world, BlockPos sourcePipe, int distance, LaserPipeNet.LaserData laserData, LaserPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.laserData = laserData;
        this.minProperties = properties;
    }

    @Nonnull
    @Override
    protected LaserNetWalker createSubWalker(LaserPipeNet world, BlockPos nextPos, int walkedBlocks) {
        LaserNetWalker walker = new LaserNetWalker(world, nextPos, walkedBlocks, laserData, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.axis = axis;
        return walker;
    }

    @Override
    protected boolean checkPipe(Node<LaserPipeNet.LaserData> pipeTile, BlockPos pos) {
        LaserPipeProperties pipeProperties = pipeTile.data.getProperties();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new LaserPipeProperties(pipeProperties);
        }
        return true;
    }

    @Override
    protected void checkNeighbour(Node<LaserPipeNet.LaserData> pipeNode, BlockPos pipePos, Direction faceToNeighbour) {
        if (pipePos.equals(sourcePipe) && faceToNeighbour == facingToHandler) {
            return;
        }

        if (laserData == null) {
            ILaserContainer handler = GTCapabilityHelper.getLaserContainer(getLevel(), pipePos, faceToNeighbour.getOpposite());
            if (handler != null) {
                laserData = new LaserPipeNet.LaserData(faceToNeighbour, new LaserPipeProperties());
            }
        }
    }
}
