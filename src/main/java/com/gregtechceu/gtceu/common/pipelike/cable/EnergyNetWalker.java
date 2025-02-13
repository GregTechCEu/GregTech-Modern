package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnergyNetWalker extends PipeNetWalker<CableBlockEntity, WireProperties, EnergyNet> {

    @Nullable
    public static List<EnergyRoutePath> createNetData(EnergyNet pipeNet, BlockPos sourcePipe) {
        try {
            EnergyNetWalker walker = new EnergyNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>());
            walker.traversePipeNet();
            return walker.routes;
        } catch (Exception e) {
            GTCEu.LOGGER.error("error while create net data for energynet", e);
        }
        return null;
    }

    private final List<EnergyRoutePath> routes;
    private CableBlockEntity[] pipes = {};
    private int loss;

    public EnergyNetWalker(EnergyNet pipeNet, BlockPos sourcePipe, int walkedBlocks, List<EnergyRoutePath> routes) {
        super(pipeNet, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @NotNull
    @Override
    protected PipeNetWalker<CableBlockEntity, WireProperties, EnergyNet> createSubWalker(EnergyNet pipeNet,
                                                                                         Direction facingToNextPos,
                                                                                         BlockPos nextPos,
                                                                                         int walkedBlocks) {
        EnergyNetWalker walker = new EnergyNetWalker(pipeNet, nextPos, walkedBlocks, routes);
        walker.loss = loss;
        walker.pipes = pipes;
        return walker;
    }

    @Override
    protected void checkPipe(CableBlockEntity pipeTile, BlockPos pos) {
        pipes = ArrayUtils.add(pipes, pipeTile);
        loss += pipeTile.getNodeData().getLossPerBlock();
    }

    @Override
    protected void checkNeighbour(CableBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {
        // assert that the last added pipe is the current pipe
        if (pipeTile != pipes[pipes.length - 1])
            throw new IllegalStateException(
                    "The current pipe is not the last added pipe. Something went seriously wrong!");
        if (neighbourTile != null) {
            IEnergyContainer container = neighbourTile
                    .getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, faceToNeighbour.getOpposite()).resolve()
                    .orElse(null);
            if (container != null) {
                routes.add(new EnergyRoutePath(pipePos.immutable(), faceToNeighbour, pipes, getWalkedBlocks(), loss));
            }
        }
    }

    @Override
    protected Class<CableBlockEntity> getBasePipeClass() {
        return CableBlockEntity.class;
    }
}
