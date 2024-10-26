package com.gregtechceu.gtceu.api.pipenet.longdistance;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LongDistanceNetwork {

    // all pipes and endpoints in this net
    private final ObjectOpenHashSet<BlockPos> longDistancePipeBlocks = new ObjectOpenHashSet<>();
    @Getter
    private final LongDistancePipeType pipeType;
    private final WorldData world;
    // stores all connected endpoints, but only the first two are being used
    private final List<ILDEndpoint> endpoints = new ArrayList<>();
    // all endpoint positions, for nbt
    private final List<BlockPos> endpointPoss = new ArrayList<>();
    private int activeInputIndex = -1, activeOutputIndex = -1;

    protected LongDistanceNetwork(LongDistancePipeType pipeType, WorldData world) {
        this.pipeType = pipeType;
        this.world = world;
    }

    @Nullable
    public static LongDistanceNetwork get(LevelAccessor world, BlockPos pos) {
        return WorldData.get(world).getNetwork(pos);
    }

    /**
     * Calculates one or more networks based on the given starting points.
     * For this it will start a new thread to keep the main thread free.
     */
    protected void recalculateNetwork(Collection<BlockPos> starts) {
        invalidateNetwork(true);
        // start a new thread where all given starting points are being walked
        new NetworkBuilder(world, this, starts).start();
    }

    /**
     * Called from the {@link NetworkBuilder} to set the gathered data
     */
    protected void setData(Collection<BlockPos> pipes, List<ILDEndpoint> endpoints) {
        invalidateEndpoints();
        this.longDistancePipeBlocks.clear();
        this.longDistancePipeBlocks.addAll(pipes);
        this.endpoints.clear();
        this.endpoints.addAll(endpoints);
        if (this.longDistancePipeBlocks.isEmpty()) {
            invalidateNetwork(false);
            return;
        }
        for (BlockPos pos : this.longDistancePipeBlocks) {
            this.world.putNetwork(pos, this);
        }
    }

    /**
     * Removes the pipe at the given position and recalculates all neighbour positions if necessary
     */
    public void onRemovePipe(BlockPos pos) {
        // remove the network from that pos
        this.longDistancePipeBlocks.remove(pos);
        this.world.removeNetwork(pos);
        if (this.longDistancePipeBlocks.isEmpty()) {
            invalidateNetwork(false);
            return;
        }
        // find amount of neighbour networks
        List<BlockPos> neighbours = new ArrayList<>();
        BlockPos.MutableBlockPos offsetPos = new BlockPos.MutableBlockPos();
        for (Direction facing : GTUtil.DIRECTIONS) {
            offsetPos.set(pos).move(facing);
            LongDistanceNetwork network = world.getNetwork(offsetPos);
            if (network == this) {
                neighbours.add(offsetPos.immutable());
            }
        }
        if (neighbours.size() > 1) {
            // the pipe had more than 1 neighbour
            // the network might need to be recalculated for each neighbour
            recalculateNetwork(neighbours);
        }
    }

    protected void addEndpoint(ILDEndpoint endpoint) {
        if (!this.endpoints.contains(endpoint)) {
            this.endpoints.add(endpoint);
        }
    }

    protected void addEndpoint(Collection<ILDEndpoint> endpoints) {
        for (ILDEndpoint endpoint : endpoints) {
            if (!this.endpoints.contains(endpoint)) {
                this.endpoints.add(endpoint);
            }
        }
    }

    public void onRemoveEndpoint(ILDEndpoint endpoint) {
        // invalidate all linked endpoints
        endpoint.invalidateLink();
        if (this.endpoints.remove(endpoint)) {
            invalidateEndpoints();
        }
        onRemovePipe(endpoint.getPos());
    }

    /**
     * Adds a new pipe to the network
     */
    public void onPlacePipe(BlockPos pos) {
        this.longDistancePipeBlocks.add(pos);
        this.world.putNetwork(pos, this);
    }

    /**
     * Adds a new endpoint to the network
     */
    public void onPlaceEndpoint(ILDEndpoint endpoint) {
        addEndpoint(endpoint);
        this.longDistancePipeBlocks.add(endpoint.getPos());
        this.world.putNetwork(endpoint.getPos(), this);
    }

    /**
     * Merge a network into this network
     */
    protected void mergePipeNet(LongDistanceNetwork network) {
        if (getPipeType() != network.getPipeType()) {
            throw new IllegalStateException("Can't merge unequal pipe types, " + getPipeType().getName() + " and " +
                    network.getPipeType().getName() + " !");
        }
        for (BlockPos pos : network.longDistancePipeBlocks) {
            this.world.putNetwork(pos, this);
            this.longDistancePipeBlocks.add(pos);
        }
        addEndpoint(network.endpoints);
        for (ILDEndpoint endpoint1 : this.endpoints) {
            endpoint1.invalidateLink();
        }
        network.invalidateNetwork(false);
    }

    /**
     * invalidate this network
     */
    protected void invalidateNetwork(boolean removeFromWorld) {
        if (removeFromWorld) {
            for (BlockPos pos : this.longDistancePipeBlocks) {
                this.world.removeNetwork(pos);
            }
        }
        this.longDistancePipeBlocks.clear();
        this.world.networkList.remove(this);
        invalidateEndpoints();
        this.endpoints.clear();
    }

    public void invalidateEndpoints() {
        this.activeInputIndex = -1;
        this.activeOutputIndex = -1;
        for (ILDEndpoint endpoint : this.endpoints) {
            endpoint.invalidateLink();
        }
    }

    /**
     * Finds the first other endpoint for the given endpoint connected to this network.
     *
     * @param endpoint endpoint to find another endpoint for
     * @return other endpoint or null if none is found
     */
    @Nullable
    public ILDEndpoint getOtherEndpoint(ILDEndpoint endpoint) {
        // return null for invalid network configurations
        if (!isValid() || (!endpoint.isInput() && !endpoint.isOutput())) return null;

        // check if endpoint really exists in this network
        int thisIndex = this.endpoints.indexOf(endpoint);
        if (thisIndex < 0) {
            // endpoint not found in this network, something is wrong, recalculate network
            recalculateNetwork(Collections.singleton(endpoint.getPos()));
            return null;
        }

        if (isIOIndexInvalid()) {
            // current endpoint indexes are invalid
            invalidateEndpoints();
        } else if (this.activeInputIndex >= 0) {
            // there is an active input and output endpoint
            ILDEndpoint in = this.endpoints.get(this.activeInputIndex);
            ILDEndpoint out = this.endpoints.get(this.activeOutputIndex);
            if (!this.pipeType.satisfiesMinLength(in, out)) {
                invalidateEndpoints();
                return getOtherEndpoint(endpoint);
            }
            if (in == endpoint) {
                if (!endpoint.isInput()) throw new IllegalStateException("Other endpoint from input was itself");
                // given endpoint is the current input, and therefore we return the output
                return out;
            }
            if (out == endpoint) {
                if (!endpoint.isOutput()) throw new IllegalStateException("Other endpoint from output was itself");
                // given endpoint is the current output, and therefore we return the input
                return in;
            }
            return null;
        }

        // find a valid endpoint in this net
        int otherIndex = find(endpoint);
        if (otherIndex >= 0) {
            // found other endpoint
            ILDEndpoint other = this.endpoints.get(otherIndex);
            // set active endpoints
            this.activeOutputIndex = endpoint.isOutput() ? thisIndex : otherIndex;
            this.activeInputIndex = endpoint.isInput() ? thisIndex : otherIndex;
            return other;
        }
        return null;
    }

    private int find(ILDEndpoint endpoint) {
        for (int i = 0; i < this.endpoints.size(); i++) {
            ILDEndpoint other = this.endpoints.get(i);
            if (other.isInValid()) {
                other.invalidateLink();
                this.endpoints.remove(i--);
                continue;
            }
            if (endpoint != other &&
                    (other.isOutput() || other.isInput()) &&
                    other.isInput() != endpoint.isInput() &&
                    this.pipeType.satisfiesMinLength(endpoint, other)) {
                // found valid endpoint with minimum distance
                return i;
            }
        }
        return -1;
    }

    public boolean isIOIndexInvalid() {
        return (this.activeInputIndex >= 0 && this.activeInputIndex >= this.endpoints.size()) ||
                (this.activeOutputIndex >= 0 && this.activeOutputIndex >= this.endpoints.size()) ||
                this.activeInputIndex < 0 != this.activeOutputIndex < 0;
    }

    public ILDEndpoint getActiveInputIndex() {
        return this.activeInputIndex >= 0 ? this.endpoints.get(this.activeInputIndex) : null;
    }

    public ILDEndpoint getActiveOutputIndex() {
        return this.activeOutputIndex >= 0 ? this.endpoints.get(this.activeOutputIndex) : null;
    }

    /**
     * @return the total amount of connected and valid ld pipe blocks and endpoints
     */
    public int getTotalSize() {
        return this.longDistancePipeBlocks.size();
    }

    /**
     * @return the total amount of connected and valid endpoints
     */
    public int getEndpointAmount() {
        return this.endpoints.size();
    }

    /**
     * @return the total amount of connected and valid ld pipe blocks
     */
    public int getPipeAmount() {
        return getTotalSize() - getEndpointAmount();
    }

    /**
     * @return if this network has more than one valid endpoint
     */
    public boolean isValid() {
        return getEndpointAmount() > 1;
    }

    /**
     * Stores all pipe data for a world/dimension
     */
    public static class WorldData extends SavedData {

        // A chunk pos to block pos to network map map
        private final Long2ObjectMap<Object2ObjectMap<BlockPos, LongDistanceNetwork>> networks = new Long2ObjectOpenHashMap<>();
        // All existing networks in this world
        private final ObjectOpenHashSet<LongDistanceNetwork> networkList = new ObjectOpenHashSet<>();
        private WeakReference<LevelAccessor> worldRef = new WeakReference<>(null);

        public WorldData() {
            super();
        }

        public static WorldData create(ServerLevel level) {
            WorldData data = new WorldData();
            data.setWorldAndInit(level);
            return data;
        }

        public static WorldData get(LevelAccessor level) {
            if (level instanceof ServerLevel serverLevel) {
                return serverLevel.getDataStorage().computeIfAbsent((tag) -> WorldData.load(tag, serverLevel),
                        () -> WorldData.create(serverLevel), "gtceu_long_dist_pipe");
            }
            return null;
        }

        private static long getChunkPos(BlockPos pos) {
            return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        }

        /**
         * set world and load all endpoints
         */
        protected void setWorldAndInit(LevelAccessor world) {
            if (this.worldRef.get() != world) {
                for (LongDistanceNetwork ld : this.networkList) {
                    if (!ld.endpointPoss.isEmpty()) {
                        ld.endpoints.clear();
                        for (BlockPos pos : ld.endpointPoss) {
                            ILDEndpoint endpoint = ILDEndpoint.tryGet(world, pos);
                            if (endpoint != null) {
                                ld.addEndpoint(endpoint);
                            }
                        }
                    }
                }
            }
            this.worldRef = new WeakReference<>(world);
            this.setDirty();
        }

        public LongDistanceNetwork getNetwork(BlockPos pos) {
            return this.networks.getOrDefault(getChunkPos(pos), Object2ObjectMaps.emptyMap()).get(pos);
        }

        private void putNetwork(BlockPos pos, LongDistanceNetwork network) {
            long chunkPos = getChunkPos(pos);
            Object2ObjectMap<BlockPos, LongDistanceNetwork> chunkNetworks = this.networks.get(chunkPos);
            if (chunkNetworks == null) {
                chunkNetworks = new Object2ObjectOpenHashMap<>();
                this.networks.put(chunkPos, chunkNetworks);
            }
            chunkNetworks.put(pos, network);
            this.networkList.add(network);
            this.setDirty();
        }

        private void removeNetwork(BlockPos pos) {
            long chunkPos = getChunkPos(pos);
            Object2ObjectMap<BlockPos, LongDistanceNetwork> chunkNetworks = this.networks.get(chunkPos);
            if (chunkNetworks != null) {
                chunkNetworks.remove(pos);
                if (chunkNetworks.isEmpty()) {
                    this.networks.remove(chunkPos);
                }
            }
            this.setDirty();
        }

        public static WorldData load(@NotNull CompoundTag nbtTagCompound, ServerLevel level) {
            WorldData data = new WorldData();
            data.networks.clear();
            data.networkList.clear();
            ListTag list = nbtTagCompound.getList("nets", Tag.TAG_COMPOUND);
            for (Tag nbt : list) {
                CompoundTag tag = (CompoundTag) nbt;
                LongDistancePipeType pipeType = LongDistancePipeType.getPipeType(tag.getString("class"));
                LongDistanceNetwork ld = pipeType.createNetwork(data);
                ld.activeInputIndex = tag.getInt("in");
                ld.activeOutputIndex = tag.getInt("out");
                data.networkList.add(ld);
                ListTag posList = tag.getList("pipes", Tag.TAG_LONG);
                for (Tag nbtPos : posList) {
                    BlockPos pos = BlockPos.of(((LongTag) nbtPos).getAsLong());
                    data.putNetwork(pos, ld);
                    ld.longDistancePipeBlocks.add(pos);
                }
                ListTag endpoints = tag.getList("endpoints", Tag.TAG_LONG);
                for (Tag nbtPos : endpoints) {
                    BlockPos pos = BlockPos.of(((LongTag) nbtPos).getAsLong());
                    if (!ld.endpointPoss.contains(pos)) {
                        ld.endpointPoss.add(pos);
                    }
                }
            }
            data.setWorldAndInit(level);
            return data;
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbtTagCompound) {
            ListTag list = new ListTag();
            for (LongDistanceNetwork network : this.networkList) {
                CompoundTag tag = new CompoundTag();
                list.add(tag);

                String name = network.getPipeType().getName();
                tag.putString("class", name);
                tag.putInt("in", network.activeInputIndex);
                tag.putInt("out", network.activeOutputIndex);

                ListTag posList = new ListTag();
                tag.put("pipes", posList);
                for (BlockPos pos : network.longDistancePipeBlocks) {
                    posList.add(LongTag.valueOf(pos.asLong()));
                }

                ListTag endpoints = new ListTag();
                tag.put("endpoints", endpoints);
                for (ILDEndpoint endpoint : network.endpoints) {
                    endpoints.add(LongTag.valueOf(endpoint.getPos().asLong()));
                }
            }
            nbtTagCompound.put("nets", list);
            return nbtTagCompound;
        }

        public LevelAccessor getWorld() {
            return this.worldRef.get();
        }
    }
}
