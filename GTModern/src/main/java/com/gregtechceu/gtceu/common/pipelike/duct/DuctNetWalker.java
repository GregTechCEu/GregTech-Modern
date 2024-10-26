package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;
import com.gregtechceu.gtceu.common.blockentity.DuctPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DuctNetWalker extends PipeNetWalker<DuctPipeBlockEntity, DuctPipeProperties, DuctPipeNet> {

    public static List<DuctRoutePath> createNetData(DuctPipeNet pipeNet, BlockPos sourcePipe, Direction sourceFacing) {
        if (!(pipeNet.getLevel().getBlockEntity(sourcePipe) instanceof DuctPipeBlockEntity)) {
            return null;
        }
        try {
            DuctNetWalker walker = new DuctNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>(), null);
            walker.sourcePipe = sourcePipe;
            walker.facingToHandler = sourceFacing;
            walker.traversePipeNet();
            return walker.inventories;
        } catch (Exception e) {
            GTCEu.LOGGER.error("error while create net data for DuctPipeNet", e);
        }
        return null;
    }

    private DuctPipeProperties minProperties;
    private final List<DuctRoutePath> inventories;
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    protected DuctNetWalker(DuctPipeNet world, BlockPos sourcePipe, int distance, List<DuctRoutePath> inventories,
                            DuctPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @NotNull
    @Override
    protected PipeNetWalker<DuctPipeBlockEntity, DuctPipeProperties, DuctPipeNet> createSubWalker(DuctPipeNet pipeNet,
                                                                                                  Direction facingToNextPos,
                                                                                                  BlockPos nextPos,
                                                                                                  int walkedBlocks) {
        DuctNetWalker walker = new DuctNetWalker(pipeNet, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected Class<DuctPipeBlockEntity> getBasePipeClass() {
        return DuctPipeBlockEntity.class;
    }

    @Override
    protected void checkPipe(DuctPipeBlockEntity pipeTile, BlockPos pos) {
        DuctPipeProperties pipeProperties = pipeTile.getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new DuctPipeProperties(
                    Math.min(minProperties.getTransferRate(), pipeProperties.getTransferRate()));
        }
    }

    @Override
    protected void checkNeighbour(DuctPipeBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {
        if ((pipePos.equals(sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        if (neighbourTile != null) {
            LazyOptional<IHazardParticleContainer> handler = neighbourTile.getCapability(
                    GTCapability.CAPABILITY_HAZARD_CONTAINER,
                    faceToNeighbour.getOpposite());
            if (handler.isPresent()) {
                inventories.add(new DuctRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks(), minProperties));
            }
        } else if (pipeTile.isConnected(faceToNeighbour)) {
            inventories.add(new DuctRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks(), minProperties));
        }
    }
}
