package com.gregtechceu.gtceu.api.pipenet.longdistance;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This bad boy is responsible for building the network
 */
public class NetworkBuilder extends Thread {

    private final ObjectList<BlockPos> starts = new ObjectArrayList<>();
    private final LongDistanceNetwork.WorldData worldData;
    private final LongDistanceNetwork originalNetwork;
    private LongDistanceNetwork network;
    private final LevelAccessor world;
    private final ObjectList<BlockPos> currentPoints = new ObjectArrayList<>();
    private final ObjectOpenHashSet<BlockPos> walked = new ObjectOpenHashSet<>();
    private final List<BlockPos> pipes = new ArrayList<>();
    private final List<ILDEndpoint> endpoints = new ArrayList<>();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final ObjectOpenHashSet<ChunkAccess> loadedChunks = new ObjectOpenHashSet<>();

    public NetworkBuilder(LongDistanceNetwork.WorldData worldData, LongDistanceNetwork network, Collection<BlockPos> starts) {
        this.worldData = Objects.requireNonNull(worldData);
        this.originalNetwork = Objects.requireNonNull(network);
        this.network = network;
        this.world = worldData.getWorld();
        this.starts.addAll(starts);
    }

    @Override
    public void run() {
        if (this.starts.isEmpty()) return;
        BlockPos start = this.starts.remove(0);
        checkNetwork(start);

        // iterate over each given starting point and try to build a network
        while (!this.starts.isEmpty()) {
            start = this.starts.remove(0);
            LongDistanceNetwork ldn = this.worldData.getNetwork(start);
            if (ldn == this.originalNetwork) {
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

        if (this.world instanceof ServerLevel serverLevel) {
            this.loadedChunks.forEach(loadedChunk -> {
                serverLevel.unload(serverLevel.getChunk(loadedChunk.getPos().x, loadedChunk.getPos().z));
            });
        }
    }

    private void checkNetwork(BlockPos start) {
        // current points stores all current branches of the network
        checkPos(this.world.getBlockState(start), start);
        while (!this.currentPoints.isEmpty()) {
            // get and remove the first stored branch
            BlockPos current = this.currentPoints.remove(0);
            for (Direction facing : Direction.values()) {
                this.pos.set(current).move(facing);
                if (this.walked.contains(this.pos)) {
                    continue;
                }
                BlockState blockState = this.getBlockState(this.pos);
                if (blockState.isAir()) {
                    continue;
                }
                checkPos(blockState, this.pos);
            }
        }
        // the whole net was checked
        // now send the data to the given network
        this.network.setData(this.pipes, this.endpoints);
    }

    /**
     * Checks a pos for a pipe or a endpoint
     */
    private void checkPos(BlockState blockState, BlockPos pos) {
        LongDistanceNetwork network = LongDistanceNetwork.get(world, pos);
        if (network != null && network != this.network) {
            network.invalidateNetwork(true);
        }
        BlockPos bp = pos.immutable();
        this.walked.add(bp);
        ILDNetworkPart part = ILDNetworkPart.tryGet(this.world, pos, blockState);
        if (part != null) {
            this.pipes.add(bp);

            if (part instanceof ILDEndpoint endpoint)
                this.endpoints.add(endpoint);
            else
                this.currentPoints.add(bp);
        }
    }

    /**
     * Special method which can get block states which are far away. It temporarily loads the chunk for that.
     */
    private BlockState getBlockState(BlockPos pos) {
        if (this.world.isOutsideBuildHeight(pos)) return Blocks.AIR.defaultBlockState();

        ChunkPos chunkPos = new ChunkPos(pos);
        ChunkSource chunkSource = this.world.getChunkSource();
        ChunkAccess chunk = chunkSource.getChunk(chunkPos.x, chunkPos.z, false);

        // chunk is not loaded, try to load it
        if (chunk == null) {
            // don't force generate a chunk
            if (!chunkSource.hasChunk(chunkPos.x, chunkPos.z)) {
                return Blocks.AIR.defaultBlockState();
            }

            chunk = Objects.requireNonNull(chunkSource.getChunk(chunkPos.x, chunkPos.z, true));
            this.loadedChunks.add(chunk);
        }

        return chunk.getBlockState(pos);
    }
}