package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.GTCEu;
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

public class FluidPipeNetWalker extends PipeNetWalker<FluidPipeData, FluidPipeNet> {

    @Nullable
    public static List<PipeNetRoutePath> createNetData(FluidPipeNet pipeNet, BlockPos sourcePipe) {
        try {
            FluidPipeNetWalker walker = new FluidPipeNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>());
            walker.traversePipeNet();
            return walker.routes;
        } catch (Exception e){
            GTCEu.LOGGER.error("error while create net data for FluidPipeNet", e);
        }
        return null;
    }

    private final List<PipeNetRoutePath> routes;
    private List<Pair<BlockPos, FluidPipeData>> pipes = new ArrayList<>();

    public FluidPipeNetWalker(FluidPipeNet pipeNet, BlockPos sourcePipe, int walkedBlocks, List<PipeNetRoutePath> routes) {
        super(pipeNet, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @NotNull
    @Override
    protected PipeNetWalker<FluidPipeData, FluidPipeNet> createSubWalker(FluidPipeNet pipeNet, BlockPos nextPos, int walkedBlocks) {
        FluidPipeNetWalker walker = new FluidPipeNetWalker(pipeNet, nextPos, walkedBlocks, routes);
        walker.pipes = new ArrayList<>(pipes);
        return walker;
    }

    @Override
    protected boolean checkPipe(Node<FluidPipeData> pipeNode, BlockPos pos) {
        pipes.add(new Pair<>(pos.immutable(), pipeNode.data));
        return true;
    }

    @Override
    protected void checkNeighbour(Node<FluidPipeData> pipeNode, BlockPos pipePos, Direction faceToNeighbour) {
        if (pipeNode.data.canAttachTo(faceToNeighbour)) {
            routes.add(new PipeNetRoutePath(pipePos.immutable(), faceToNeighbour, pipes.toArray(Pair[]::new), getWalkedBlocks()));
        }
    }

}
