package com.gregtechceu.gtceu.api.pipenet.longdist;

import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LongDistanceNetwork {

    // all pipes and endpoints in this net
    private final ObjectOpenHashSet<BlockPos> longDistancePipeBlocks = new ObjectOpenHashSet<>();
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
    public static LongDistanceNetwork get(World world, BlockPos pos) {
        return WorldData.get(world).getNetwork(pos);
    }

    /**
     * Calculates one or more networks based on the given starting points.
     * For this it will start a new thread to keep the main thread free.
     */
    protected void recalculateNetwork(Collection<BlockPos> starts) {
        // remove the every pipe from the network
        for (BlockPos pos : this.longDistancePipeBlocks) {
            this.world.removeNetwork(pos);
        }
        invalidateEndpoints();
        this.endpoints.clear();
        this.longDistancePipeBlocks.clear();
        // start a new thread where all given starting points are being walked
        Thread thread = new Thread(new NetworkBuilder(world, this, starts));
        thread.start();
    }

    /**
     * Called from the {@link NetworkBuilder} to set the gathered data
     */
    protected void setData(Collection<BlockPos> pipes, List<ILDEndpoint> endpoints) {
        boolean wasEmpty = this.longDistancePipeBlocks.isEmpty();
        this.longDistancePipeBlocks.clear();
        this.longDistancePipeBlocks.addAll(pipes);
        this.endpoints.clear();
        this.endpoints.addAll(endpoints);
        if (this.longDistancePipeBlocks.isEmpty()) {
            invalidateNetwork();
            return;
        }
        if (wasEmpty) {
            this.world.networkList.add(this);
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
            invalidateNetwork();
            return;
        }
        // find amount of neighbour networks
        List<BlockPos> neighbours = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(pos).move(facing);
            LongDistanceNetwork network = world.getNetwork(offsetPos);
            if (network == this) {
                neighbours.add(offsetPos.toImmutable());
            }
        }
        offsetPos.release();
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
            throw new IllegalStateException("Can't merge unequal pipe types, " + getPipeType().getName() + " and " + network.getPipeType().getName() + " !");
        }
        for (BlockPos pos : network.longDistancePipeBlocks) {
            this.world.putNetwork(pos, this);
            this.longDistancePipeBlocks.add(pos);
        }
        addEndpoint(network.endpoints);
        for (ILDEndpoint endpoint1 : this.endpoints) {
            endpoint1.invalidateLink();
        }
        network.invalidateNetwork();
    }

    /**
     * invalidate this network
     */
    protected void invalidateNetwork() {
        this.longDistancePipeBlocks.clear();
        this.world.networkList.remove(this);
        invalidateEndpoints();
        this.endpoints.clear();
    }

    protected void invalidateEndpoints() {
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

        if (this.activeInputIndex >= 0 && this.activeOutputIndex >= 0) {
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
        } else if (this.activeInputIndex < 0 != this.activeOutputIndex < 0) {
            // only input or output is active
            GTLog.logger.warn("Long Distance Network has an {}. This should not happen!", this.activeInputIndex < 0 ? "active input, but not an active output" : "active output, but not an active input");
            invalidateEndpoints(); // shouldn't happen
        }

        // find a valid endpoint in this net
        int otherIndex = find(endpoint);
        if (otherIndex >= 0) {
            // found other endpoint
            int thisIndex = this.endpoints.indexOf(endpoint);
            if (thisIndex < 0)
                throw new IllegalStateException("Tried to get endpoint that is not part of this network. Something is seriously wrong!");
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

    public LongDistancePipeType getPipeType() {
        return pipeType;
    }

    /**
     * Stores all pipe data for a world/dimension
     */
    public static class WorldData extends WorldSavedData {

        private static final Object2ObjectOpenHashMap<World, WorldData> WORLD_DATA_MAP = new Object2ObjectOpenHashMap<>();

        // A chunk pos to block pos to network map map
        private final Long2ObjectMap<Object2ObjectMap<BlockPos, LongDistanceNetwork>> networks = new Long2ObjectOpenHashMap<>();
        // All existing networks in this world
        private final ObjectOpenHashSet<LongDistanceNetwork> networkList = new ObjectOpenHashSet<>();
        private WeakReference<World> worldRef = new WeakReference<>(null);

        public WorldData(String name) {
            super(name);
        }

        public static WorldData get(World world) {
            WorldData worldData = WORLD_DATA_MAP.get(world);
            if (worldData != null) {
                return worldData;
            }
            String DATA_ID = WorldPipeNet.getDataID("long_dist_pipe", world);
            WorldData netWorldData = (WorldData) world.loadData(WorldData.class, DATA_ID);
            if (netWorldData == null) {
                netWorldData = new WorldData(DATA_ID);
                world.setData(DATA_ID, netWorldData);
                WORLD_DATA_MAP.put(world, netWorldData);
            }
            netWorldData.setWorldAndInit(world);
            return netWorldData;
        }

        private static long getChunkPos(BlockPos pos) {
            return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        }

        /**
         * set world and load all endpoints
         */
        protected void setWorldAndInit(World world) {
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
        }

        @Override
        public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
            this.networks.clear();
            this.networkList.clear();
            NBTTagList list = nbtTagCompound.getTagList("nets", Constants.NBT.TAG_COMPOUND);
            for (NBTBase nbt : list) {
                NBTTagCompound tag = (NBTTagCompound) nbt;
                LongDistancePipeType pipeType = LongDistancePipeType.getPipeType(tag.getString("class"));
                LongDistanceNetwork ld = pipeType.createNetwork(this);
                ld.activeInputIndex = tag.getInteger("in");
                ld.activeOutputIndex = tag.getInteger("out");
                this.networkList.add(ld);
                NBTTagList posList = tag.getTagList("pipes", Constants.NBT.TAG_LONG);
                for (NBTBase nbtPos : posList) {
                    BlockPos pos = BlockPos.fromLong(((NBTTagLong) nbtPos).getLong());
                    putNetwork(pos, ld);
                    ld.longDistancePipeBlocks.add(pos);
                }
                NBTTagList endpoints = tag.getTagList("endpoints", Constants.NBT.TAG_LONG);
                for (NBTBase nbtPos : endpoints) {
                    BlockPos pos = BlockPos.fromLong(((NBTTagLong) nbtPos).getLong());
                    if (!ld.endpointPoss.contains(pos)) {
                        ld.endpointPoss.add(pos);
                    }
                }
            }
        }

        @NotNull
        @Override
        public NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbtTagCompound) {
            NBTTagList list = new NBTTagList();
            for (LongDistanceNetwork network : this.networkList) {
                NBTTagCompound tag = new NBTTagCompound();
                list.appendTag(tag);

                String name = network.getPipeType().getName();
                tag.setString("class", name);
                tag.setInteger("in", network.activeInputIndex);
                tag.setInteger("out", network.activeOutputIndex);

                NBTTagList posList = new NBTTagList();
                tag.setTag("pipes", posList);
                for (BlockPos pos : network.longDistancePipeBlocks) {
                    posList.appendTag(new NBTTagLong(pos.toLong()));
                }

                NBTTagList endpoints = new NBTTagList();
                tag.setTag("endpoints", endpoints);
                for (ILDEndpoint endpoint : network.endpoints) {
                    endpoints.appendTag(new NBTTagLong(endpoint.getPos().toLong()));
                }
            }
            nbtTagCompound.setTag("nets", list);
            return nbtTagCompound;
        }

        public World getWorld() {
            return this.worldRef.get();
        }
    }
}
