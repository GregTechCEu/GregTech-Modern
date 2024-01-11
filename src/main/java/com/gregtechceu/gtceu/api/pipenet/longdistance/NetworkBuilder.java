package com.gregtechceu.gtceu.api.pipenet.longdistance;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This bad boy is responsible for building the network
 */
public class NetworkBuilder implements Runnable {

    private final LinkedList<BlockPos> starts = new LinkedList<>();
    private final LongDistanceNetwork.WorldData worldData;
    private LongDistanceNetwork network;
    private final LevelAccessor world;
    private final LinkedList<BlockPos> currentPoints = new LinkedList<>();
    private final ObjectOpenHashSet<BlockPos> walked = new ObjectOpenHashSet<>();
    private final List<BlockPos> pipes = new ArrayList<>();
    private final List<ILDEndpoint> endpoints = new ArrayList<>();

    public NetworkBuilder(LongDistanceNetwork.WorldData worldData, LongDistanceNetwork network, BlockPos start) {
        this.worldData = worldData;
        this.network = network;
        this.world = worldData.getWorld();
        this.starts.add(start);
    }

    public NetworkBuilder(LongDistanceNetwork.WorldData worldData, LongDistanceNetwork network, Collection<BlockPos> starts) {
        this.worldData = worldData;
        this.network = network;
        this.world = worldData.getWorld();
        this.starts.addAll(starts);
    }

    @Override
    public void run() {
        // iterate over each given starting point and try to build a network
        boolean first = true;
        while (!starts.isEmpty()) {
            BlockPos start = starts.pollFirst();
            if (first) {
                first = false;
                checkNetwork(start);
                continue;
            }
            LongDistanceNetwork ldn = worldData.getNetwork(start);
            if (ldn != null) {
                // this starting point was caught during a previous iteration, so we don't need to create another network here
                continue;
            }
            // create a new network, since the current was already calculated
            this.network = this.network.getPipeType().createNetwork(this.worldData);
            this.currentPoints.clear();
            this.walked.clear();
            this.pipes.clear();
            this.endpoints.clear();
            checkNetwork(start);
        }
    }

    private void checkNetwork(BlockPos start) {
        // current points stores all current branches of the network
        this.currentPoints.add(start);
        checkPos(world.getBlockState(start), start);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while (!currentPoints.isEmpty()) {
            // get and remove the first stored branch
            BlockPos current = currentPoints.pollFirst();
            for (Direction facing : Direction.values()) {
                pos.set(current).move(facing);
                if (walked.contains(pos)) {
                    continue;
                }
                BlockState blockState = world.getBlockState(pos);
                if (blockState.isAir()) {
                    continue;
                }
                checkPos(blockState, pos);
            }
        }
        // the whole net was checked
        // now send the data to the given network
        network.setData(pipes, endpoints);
    }

    /**
     * Checks a pos for a pipe or a endpoint
     */
    private void checkPos(BlockState blockState, BlockPos pos) {
        BlockPos bp = pos.immutable();
        if (blockState.getBlock() instanceof LongDistancePipeBlock && network.getPipeType().isValidBlock(blockState)) {
            pipes.add(bp);
            // add another branch/block for processing
            currentPoints.addLast(bp);
        } else {
            ILDEndpoint endpoint = ILDEndpoint.tryGet(world, pos);
            if (endpoint != null && network.getPipeType().isValidEndpoint(endpoint)) {
                pipes.add(bp);
                endpoints.add(endpoint);
            }
        }
        walked.add(bp);
    }
}