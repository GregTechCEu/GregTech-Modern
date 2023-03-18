package com.gregtechceu.gtceu.common.pipelike.cable;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNetWalker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnergyNetWalker extends PipeNetWalker<CableData, EnergyNet> {

    @Nullable
    public static List<CableRoutePath> createNetData(EnergyNet pipeNet, BlockPos sourcePipe) {
        try {
            EnergyNetWalker walker = new EnergyNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>());
            walker.traversePipeNet();
            return walker.routes;
        } catch (Exception e){
            LDLib.LOGGER.error("error while create net data for energynet", e);
        }
        return null;
    }

    private final List<CableRoutePath> routes;
    private List<Pair<BlockPos, CableData>> pipes = new ArrayList<>();
    private int loss;

    public EnergyNetWalker(EnergyNet pipeNet, BlockPos sourcePipe, int walkedBlocks, List<CableRoutePath> routes) {
        super(pipeNet, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @NotNull
    @Override
    protected PipeNetWalker<CableData, EnergyNet> createSubWalker(EnergyNet pipeNet, BlockPos nextPos, int walkedBlocks) {
        EnergyNetWalker walker = new EnergyNetWalker(pipeNet, nextPos, walkedBlocks, routes);
        walker.loss = loss;
        walker.pipes = new ArrayList<>(pipes);
        return walker;
    }

    @Override
    protected boolean checkPipe(Node<CableData> pipeNode, BlockPos pos) {
        pipes.add(new Pair<>(pos.immutable(), pipeNode.data));
        loss += pipeNode.data.properties().getLossPerBlock();
        return true;
    }

    @Override
    protected void checkNeighbour(Node<CableData> pipeNode, BlockPos pipePos, Direction faceToNeighbour) {
        if (pipeNode.data.canAttachTo(faceToNeighbour)) {
            routes.add(new CableRoutePath(pipePos.immutable(), faceToNeighbour, pipes.toArray(Pair[]::new), getWalkedBlocks(), loss));
        }
    }

}
