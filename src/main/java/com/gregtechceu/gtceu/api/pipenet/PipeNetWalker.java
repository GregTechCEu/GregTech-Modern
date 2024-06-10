package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This is a helper class to get information about a pipe net
 * <p>
 * The walker is written that it will always find the shortest path to any destination
 * <p>
 * On the way it can collect information about the pipes and it's neighbours
 * <p>
 * After creating a walker simply call {@link #traversePipeNet()} to start walking, then you can just collect the data
 * <p>
 * <b>Do not walk a walker more than once</b>
 */
@SuppressWarnings("unused")
public abstract class PipeNetWalker<T extends IPipeNode<?, ?>, NodeDataType, Net extends PipeNet<NodeDataType>> {

    protected PipeNetWalker<T, NodeDataType, Net> root;
    protected final Net pipeNet;
    private Set<T> walked;
    protected List<PipeNetWalker<T, NodeDataType, Net>> walkers;
    protected final BlockPos.MutableBlockPos currentPos;
    protected final List<Direction> nextPipeFacings = new ArrayList<>(5);
    protected final List<T> nextPipes = new ArrayList<>(5);
    protected T currentPipe;
    private Direction from = null;
    @Getter
    protected int walkedBlocks;
    @Getter
    protected boolean invalid;
    protected boolean running;
    @Getter
    private boolean failed = false;

    protected PipeNetWalker(Net pipeNet, BlockPos sourcePipe, int walkedBlocks) {
        this.pipeNet = pipeNet;
        this.walkedBlocks = walkedBlocks;
        this.currentPos = sourcePipe.mutable();
        this.root = this;
    }

    /**
     * Creates a sub walker
     * Will be called when a pipe has multiple valid pipes
     *
     * @param pipeNet      pipe net
     * @param nextPos      next pos to check
     * @param walkedBlocks distance from source in blocks
     * @return new sub walker
     */
    @NotNull
    protected abstract PipeNetWalker<T, NodeDataType, Net> createSubWalker(Net pipeNet, Direction facingToNextPos,
                                                                           BlockPos nextPos, int walkedBlocks);

    /**
     * Checks the neighbour of the current pos
     *
     * @param pipePos         current pos. Note!! its a mutable pos.
     * @param faceToNeighbour face to neighbour
     * @param pipeNode        pipeNode
     * @param neighbourTile   the neighboring BlockEntity. Might not be a pipe.
     */
    protected void checkNeighbour(T pipeNode, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {}

    /**
     * If the pipe is valid to perform a walk on
     *
     * @param currentPipe     current pipe
     * @param neighbourPipe   neighbour pipe to check
     * @param pipePos         current pos (tile.getPipePos() != pipePos)
     * @param faceToNeighbour face to pipeTile
     * @return if the pipe is valid
     */
    protected boolean isValidPipe(T currentPipe, T neighbourPipe, BlockPos pipePos, Direction faceToNeighbour) {
        return true;
    }

    protected abstract Class<T> getBasePipeClass();

    /**
     * You can increase walking stats here. for example
     *
     * @param pipeTile current checking pipe
     * @param pos      current pipe pos
     */
    protected abstract void checkPipe(T pipeTile, BlockPos pos);

    /**
     * The directions that this net can traverse from this pipe
     *
     * @return the array of valid Directions
     */
    protected Direction[] getSurroundingPipeSides() {
        return GTUtil.DIRECTIONS;
    }

    /**
     * Called when a sub walker is done walking
     *
     * @param subWalker the finished sub walker
     */
    protected void onRemoveSubWalker(PipeNetWalker<T, NodeDataType, Net> subWalker) {}

    public void traversePipeNet() {
        traversePipeNet(32768);
    }

    /**
     * Starts walking the pipe net and gathers information.
     *
     * @param maxWalks max walks to prevent possible stack overflow
     * @throws IllegalStateException if the walker already walked
     */
    public void traversePipeNet(int maxWalks) {
        if (invalid)
            throw new IllegalStateException("This walker already walked. Create a new one if you want to walk again");
        root = this;
        walked = new ObjectOpenHashSet<>();
        int i = 0;
        running = true;
        while (running && !walk() && i++ < maxWalks);
        running = false;
        root.walked.clear();
        if (i >= maxWalks)
            GTCEu.LOGGER.warn("The walker reached the maximum amount of walks {}", i);
        invalid = true;
    }

    private boolean walk() {
        if (walkers == null) {
            if (!checkPos()) {
                this.root.failed = true;
                return true;
            }

            if (nextPipeFacings.isEmpty())
                return true;
            if (nextPipeFacings.size() == 1) {
                currentPos.set(nextPipes.get(0).getPipePos());
                currentPipe = nextPipes.get(0);
                from = nextPipeFacings.get(0).getOpposite();
                walkedBlocks++;
                return !isRunning();
            }

            walkers = new ArrayList<>();
            for (int i = 0; i < nextPipeFacings.size(); i++) {
                Direction side = nextPipeFacings.get(i);
                PipeNetWalker<T, NodeDataType, Net> walker = Objects.requireNonNull(
                        createSubWalker(pipeNet, side, currentPos.relative(side), walkedBlocks + 1),
                        "Walker can't be null");
                walker.root = root;
                walker.currentPipe = nextPipes.get(i);
                walker.from = side.getOpposite();
                walkers.add(walker);
            }
        }
        Iterator<PipeNetWalker<T, NodeDataType, Net>> iterator = walkers.iterator();
        while (iterator.hasNext()) {
            PipeNetWalker<T, NodeDataType, Net> walker = iterator.next();
            if (walker.walk()) {
                onRemoveSubWalker(walker);
                iterator.remove();
            }
        }

        return !isRunning() || walkers.isEmpty();
    }

    private boolean checkPos() {
        nextPipeFacings.clear();
        nextPipes.clear();
        if (currentPipe == null) {
            BlockEntity thisPipe = getLevel().getBlockEntity(currentPos);
            if (!(thisPipe instanceof IPipeNode<?, ?>)) {
                GTCEu.LOGGER.error("PipeWalker expected a pipe, but found {} at {}", thisPipe, currentPos);
                return false;
            }
            if (!getBasePipeClass().isAssignableFrom(thisPipe.getClass())) {
                return false;
            }
            currentPipe = (T) thisPipe;
        }
        T pipeTile = currentPipe;
        checkPipe(pipeTile, currentPos);
        root.walked.add(pipeTile);

        // check for surrounding pipes and item handlers
        for (Direction accessSide : getSurroundingPipeSides()) {
            // skip sides reported as blocked by pipe network
            if (accessSide == from || !pipeTile.isConnected(accessSide))
                continue;

            BlockEntity tile = pipeTile.getNeighbor(accessSide);
            if (tile != null && getBasePipeClass().isAssignableFrom(tile.getClass())) {
                T otherPipe = (T) tile;
                if (!otherPipe.isConnected(accessSide.getOpposite()) ||
                        otherPipe.isBlocked(accessSide.getOpposite()) || isWalked(otherPipe))
                    continue;
                if (isValidPipe(pipeTile, otherPipe, currentPos, accessSide)) {
                    nextPipeFacings.add(accessSide);
                    nextPipes.add(otherPipe);
                    continue;
                }
            }
            checkNeighbour(pipeTile, currentPos, accessSide, tile);
        }
        return true;
    }

    protected boolean isWalked(T pipe) {
        return root.walked.contains(pipe);
    }

    /**
     * Will cause the root walker to stop after the next walk
     */
    public void stop() {
        root.running = false;
    }

    public boolean isRunning() {
        return root.running;
    }

    public ServerLevel getLevel() {
        return pipeNet.getLevel();
    }

    public BlockPos getCurrentPos() {
        return currentPos.immutable();
    }
}
