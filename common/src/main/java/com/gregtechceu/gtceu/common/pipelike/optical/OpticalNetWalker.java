package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNetWalker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class OpticalNetWalker extends PipeNetWalker<OpticalPipeData, OpticalPipeNet> {

    @Nullable
    public static OpticalPipeNet.OpticalInventory createNetData(OpticalPipeNet world, BlockPos sourcePipe, Direction faceToSourceHandler) {
        try {
            OpticalNetWalker walker = new OpticalNetWalker(world, sourcePipe, 1, null, null);
            walker.sourcePipe = sourcePipe;
            walker.facingToHandler = faceToSourceHandler;
            walker.traversePipeNet();
            return walker.inventory;
        } catch (Exception e){
            GTCEu.LOGGER.error("error while create net data for OpticalPipeNet", e);
        }
        return null;
    }

    private OpticalPipeData minProperties;
    private OpticalPipeNet.OpticalInventory inventory;
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    protected OpticalNetWalker(OpticalPipeNet world, BlockPos sourcePipe, int distance, OpticalPipeNet.OpticalInventory inventory, OpticalPipeData properties) {
        super(world, sourcePipe, distance);
        this.inventory = inventory;
        this.minProperties = properties;
    }

    @NotNull
    @Override
    protected PipeNetWalker<OpticalPipeData, OpticalPipeNet> createSubWalker(OpticalPipeNet pipeNet, BlockPos nextPos, int walkedBlocks) {
        OpticalNetWalker walker = new OpticalNetWalker(pipeNet, nextPos, walkedBlocks, inventory, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected boolean checkPipe(Node<OpticalPipeData> pipeNode, BlockPos pos) {
        OpticalPipeData pipeProperties = pipeNode.data;
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new OpticalPipeData();
        }
        return true;
    }

    @Override
    protected void checkNeighbour(Node<OpticalPipeData> pipeNode, BlockPos pipePos, Direction faceToNeighbour) {
        if (sourcePipe.equals(pipePos) && faceToNeighbour == facingToHandler) {
            return;
        }

        if (inventory == null) {
            if (GTCapabilityHelper.getDataAccess(getLevel(), pipePos, faceToNeighbour.getOpposite()) != null ||
                    GTCapabilityHelper.getComputationProvider(getLevel(), pipePos, faceToNeighbour.getOpposite()) != null) {
                inventory = new OpticalPipeNet.OpticalInventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties);
            }
        }
    }
}
