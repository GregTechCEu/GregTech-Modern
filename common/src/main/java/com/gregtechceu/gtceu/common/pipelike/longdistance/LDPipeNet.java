package com.gregtechceu.gtceu.common.pipelike.longdistance;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeData;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public class LDPipeNet extends PipeNet<LDPipeData> {

    // stores all connected endpoints, but only the first two are being used
    private final List<ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType>> endpoints = new ArrayList<>();
    private int activeInputIndex = -1, activeOutputIndex = -1;

    private final long lastUpdate;

    public LDPipeNet(LevelPipeNet<LDPipeData, ? extends PipeNet> world) {
        super(world);
        lastUpdate = world.getWorld().getGameTime();
    }

    @Override
    protected void addNodeSilently(BlockPos nodePos, Node<LDPipeData> node) {
        if (node.data.properties.getNodeType() != LDPipeProperties.NodeType.PIPE) {
            addEndpoint(nodePos, node.data.properties.getNodeType());
        }
        super.addNodeSilently(nodePos, node);
    }

    protected void addEndpoint(BlockPos pos, LDPipeProperties.NodeType endpoint) {
        if (endpoints.stream().noneMatch(pair -> pair.left() == pos))
            endpoints.add(new ObjectObjectImmutablePair<>(pos, endpoint));
    }

    protected void addEndpoint(ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> endpoint) {
        if (!endpoints.contains(endpoint)) {
            endpoints.add(endpoint);
        }
    }

    protected void addEndpoint(List<ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType>> endpoints) {
        for (var endpoint : endpoints) {
            addEndpoint(endpoint.left(), endpoint.right());
        }
    }

    public void onRemoveEndpoint(ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> endpoint) {
        // invalidate all linked endpoints
//        pos.invalidateLink();
        if (!this.endpoints.remove(endpoint)) {
            invalidateEndpoints();
        }
        this.removeNodeWithoutRebuilding(endpoint.left());
    }

    /**
     * Adds a new endpoint to the network
     */
    public void onPlaceEndpoint(ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> endpoint) {
        addNodeSilently(endpoint.left(), new Node<LDPipeData>(new LDPipeData(new LDPipeProperties(endpoint.right())), Node.ALL_OPENED, Node.DEFAULT_MARK, true));
    }

    protected void invalidateEndpoints() {
        this.activeInputIndex = -1;
        this.activeOutputIndex = -1;
//        for (ILDEndpoint endpoint : this.endpoints) {
//            endpoint.invalidateLink();
//        }
    }

    @Override
    protected void writeNodeData(LDPipeData nodeData, CompoundTag tagCompound) {
        tagCompound.putString("node_type", nodeData.properties.getNodeType().toString());
    }

    @Override
    protected LDPipeData readNodeData(CompoundTag tagCompound) {
        LDPipeProperties.NodeType nodeType = LDPipeProperties.NodeType.valueOf(tagCompound.getString("node_type"));
        return new LDPipeData(new LDPipeProperties(nodeType));
    }

    private boolean isValidEndpoint(ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> endpoint) {
        return endpoints.contains(endpoint);
    }

    private boolean satisfiesMinLength(BlockPos endpoint1, BlockPos endpoint2) {
        return endpoint1 != endpoint2 && endpoint1.distManhattan(endpoint2) >= getMinLength();
    }

    /**
     * Finds the first other endpoint for the given endpoint connected to this network.
     *
     * @param endpoint endpoint to find another endpoint for
     * @return other endpoint or null if none is found
     */
    @Nullable
    public ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> getOtherEndpoint(ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> endpoint) {
        // return null for invalid network configurations
        if (!isValid() || isValidEndpoint(endpoint)) return null;

        if (this.activeInputIndex >= 0 && this.activeOutputIndex >= 0) {
            // there is an active input and output endpoint
            var in = this.endpoints.get(this.activeInputIndex);
            var out = this.endpoints.get(this.activeOutputIndex);
            // TODO: customizable minimum distance between endpoints (not pipe length)
            if (!satisfiesMinLength(in.left(), out.left())) {
                invalidateEndpoints();
                return getOtherEndpoint(endpoint);
            }
            if (in == endpoint) {
                if (endpoint.right() != LDPipeProperties.NodeType.IN)
                    throw new IllegalStateException("Other endpoint from input was itself");
                // given endpoint is the current input, and therefore we return the output
                return out;
            }
            if (out == endpoint) {
                if (endpoint.right() != LDPipeProperties.NodeType.OUT) throw new IllegalStateException("Other endpoint from output was itself");
                // given endpoint is the current output, and therefore we return the input
                return in;
            }
            return null;
        } else if (this.activeInputIndex < 0 != this.activeOutputIndex < 0) {
            // only input or output is active
            GTCEu.LOGGER.warn("Long Distance Network has an {}. This should not happen!", this.activeInputIndex < 0 ? "active input, but not an active output" : "active output, but not an active input");
            invalidateEndpoints(); // shouldn't happen
        }

        // find a valid endpoint in this net
        int otherIndex = find(endpoint);
        if (otherIndex >= 0) {
            // found other endpoint
            var thisIndex = this.endpoints.indexOf(endpoint);
            if (thisIndex < 0) {
                throw new IllegalStateException("Tried to get endpoint that is not part of this network. Something is seriously wrong!");
            }
            // set active endpoints
            var other = this.endpoints.get(otherIndex);
            this.activeOutputIndex = endpoint.right() == LDPipeProperties.NodeType.OUT ? thisIndex : otherIndex;
            this.activeInputIndex = endpoint.right() == LDPipeProperties.NodeType.IN ? thisIndex : otherIndex;
            return other;
        }
        return null;
    }

    private int find(ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> endpoint) {
        for (int i = 0; i < this.endpoints.size(); i++) {
            var other = this.endpoints.get(i);
            if (endpoint != other &&
                    endpoint.right() != other.right() &&
                    satisfiesMinLength(endpoint.left(), other.left())) {
                // found valid endpoint with minimum distance
                return i;
            }
        }
        return -1;
    }

    public ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> getActiveInputIndex() {
        return this.activeInputIndex >= 0 ? this.endpoints.get(this.activeInputIndex) : null;
    }

    public ObjectObjectImmutablePair<BlockPos, LDPipeProperties.NodeType> getActiveOutputIndex() {
        return this.activeOutputIndex >= 0 ? this.endpoints.get(this.activeOutputIndex) : null;
    }

    /**
     * @return the total amount of connected and valid ld pipe blocks and endpoints
     */
    public int getTotalSize() {
        return this.getAllNodes().size();
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

//    public LDPipeType getPipeType() {
//        return pipeType;
//    }

//    /**
//     * Checks if the given block state is a valid ld pipe block for this type
//     *
//     * @param blockState potential ld pipe block
//     * @return if the given block state is a valid ld pipe block for this type
//     */
//    public abstract boolean isValidBlock(IBlockState blockState);

//    /**
//     * Checks if the given endpoint is a valid endpoint for this type
//     *
//     * @param endpoint potential endpoint
//     * @return if the given endpoint is a valid endpoint for this type
//     */
//    public abstract boolean isValidEndpoint(ILDEndpoint endpoint);

    /**
     * @return The minimum required distance (not pipe count between endpoints) between to endpoints to work.
     */
    public int getMinLength() {
        return 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        var compoundTag = new CompoundTag();

        compoundTag.putInt("in", activeInputIndex);
        compoundTag.putInt("out", activeOutputIndex);

        var endpointTags = new ListTag();

        for (var endpoint : endpoints) {
            var endpointTag = new CompoundTag();
            var nodePos = endpoint.left();
            endpointTag.putInt("x", nodePos.getX());
            endpointTag.putInt("y", nodePos.getY());
            endpointTag.putInt("z", nodePos.getZ());
            endpointTag.putString("type", endpoint.right().toString());
            endpointTags.add(endpointTag);
        }

        compoundTag.put("EndPoints", endpointTags);

        compoundTag.merge(super.serializeNBT());

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);

        this.activeInputIndex = nbt.getInt("in");
        this.activeOutputIndex = nbt.getInt("out");

        ListTag endpointList = nbt.getList("EndPoints", Tag.TAG_COMPOUND);

        for (int i = 0; i < endpointList.size(); i++) {
            CompoundTag endpointTag = endpointList.getCompound(i);
            int x = endpointTag.getInt("x");
            int y = endpointTag.getInt("y");
            int z = endpointTag.getInt("z");
            BlockPos blockPos = new BlockPos(x, y, z);
            LDPipeProperties.NodeType nodeType = LDPipeProperties.NodeType.valueOf(endpointTag.getString("type"));

            this.addEndpoint(blockPos, nodeType);
        }

    }

    //    /**
//     * Stores all pipe data for a world/dimension
//     */
//    public static class WorldData extends WorldSavedData {
//
//        private static final Object2ObjectOpenHashMap<World, WorldData> WORLD_DATA_MAP = new Object2ObjectOpenHashMap<>();
//
//        // A chunk pos to block pos to network map map
//        private final Long2ObjectMap<Object2ObjectMap<BlockPos, LDPipeNet>> networks = new Long2ObjectOpenHashMap<>();
//        // All existing networks in this world
//        private final ObjectOpenHashSet<LDPipeNet> networkList = new ObjectOpenHashSet<>();
//        private WeakReference<World> worldRef = new WeakReference<>(null);
//
//        public WorldData(String name) {
//            super(name);
//        }
//
//        public static WorldData get(World world) {
//            WorldData worldData = WORLD_DATA_MAP.get(world);
//            if (worldData != null) {
//                return worldData;
//            }
//            String DATA_ID = WorldPipeNet.getDataID("long_dist_pipe", world);
//            WorldData netWorldData = (WorldData) world.loadData(WorldData.class, DATA_ID);
//            if (netWorldData == null) {
//                netWorldData = new WorldData(DATA_ID);
//                world.setData(DATA_ID, netWorldData);
//                WORLD_DATA_MAP.put(world, netWorldData);
//            }
//            netWorldData.setWorldAndInit(world);
//            return netWorldData;
//        }
//
//        private static long getChunkPos(BlockPos pos) {
//            return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
//        }
//
//        /**
//         * set world and load all endpoints
//         */
//        protected void setWorldAndInit(World world) {
//            if (this.worldRef.get() != world) {
//                for (LDPipeNet ld : this.networkList) {
//                    if (!ld.endpointPoss.isEmpty()) {
//                        ld.endpoints.clear();
//                        for (BlockPos pos : ld.endpointPoss) {
//                            ILDEndpoint endpoint = ILDEndpoint.tryGet(world, pos);
//                            if (endpoint != null) {
//                                ld.addEndpoint(endpoint);
//                            }
//                        }
//                    }
//                }
//            }
//            this.worldRef = new WeakReference<>(world);
//        }
//
//        public LDPipeNet getNetwork(BlockPos pos) {
//            return this.networks.getOrDefault(getChunkPos(pos), Object2ObjectMaps.emptyMap()).get(pos);
//        }
//
//        private void putNetwork(BlockPos pos, LDPipeNet network) {
//            long chunkPos = getChunkPos(pos);
//            Object2ObjectMap<BlockPos, LDPipeNet> chunkNetworks = this.networks.get(chunkPos);
//            if (chunkNetworks == null) {
//                chunkNetworks = new Object2ObjectOpenHashMap<>();
//                this.networks.put(chunkPos, chunkNetworks);
//            }
//            chunkNetworks.put(pos, network);
//            this.networkList.add(network);
//        }
//
//        private void removeNetwork(BlockPos pos) {
//            long chunkPos = getChunkPos(pos);
//            Object2ObjectMap<BlockPos, LDPipeNet> chunkNetworks = this.networks.get(chunkPos);
//            if (chunkNetworks != null) {
//                chunkNetworks.remove(pos);
//                if (chunkNetworks.isEmpty()) {
//                    this.networks.remove(chunkPos);
//                }
//            }
//        }
//
//        @Override
//        public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
//            this.networks.clear();
//            this.networkList.clear();
//            NBTTagList list = nbtTagCompound.getTagList("nets", Constants.NBT.TAG_COMPOUND);
//            for (NBTBase nbt : list) {
//                NBTTagCompound tag = (NBTTagCompound) nbt;
//                LDPipeType pipeType = LDPipeType.getPipeType(tag.getString("class"));
//                LDPipeNet ld = pipeType.createNetwork(this);
//                ld.activeInputIndex = tag.getInteger("in");
//                ld.activeOutputIndex = tag.getInteger("out");
//                this.networkList.add(ld);
//                NBTTagList posList = tag.getTagList("pipes", Constants.NBT.TAG_LONG);
//                for (NBTBase nbtPos : posList) {
//                    BlockPos pos = BlockPos.fromLong(((NBTTagLong) nbtPos).getLong());
//                    putNetwork(pos, ld);
//                    ld.longDistancePipeBlocks.add(pos);
//                }
//                NBTTagList endpoints = tag.getTagList("endpoints", Constants.NBT.TAG_LONG);
//                for (NBTBase nbtPos : endpoints) {
//                    BlockPos pos = BlockPos.fromLong(((NBTTagLong) nbtPos).getLong());
//                    if (!ld.endpointPoss.contains(pos)) {
//                        ld.endpointPoss.add(pos);
//                    }
//                }
//            }
//        }
//
//        @NotNull
//        @Override
//        public NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbtTagCompound) {
//            NBTTagList list = new NBTTagList();
//            for (LDPipeNet network : this.networkList) {
//                NBTTagCompound tag = new NBTTagCompound();
//                list.appendTag(tag);
//
//                String name = network.getPipeType().getName();
//                tag.setString("class", name);
//                tag.setInteger("in", network.activeInputIndex);
//                tag.setInteger("out", network.activeOutputIndex);
//
//                NBTTagList posList = new NBTTagList();
//                tag.setTag("pipes", posList);
//                for (BlockPos pos : network.longDistancePipeBlocks) {
//                    posList.appendTag(new NBTTagLong(pos.toLong()));
//                }
//
//                NBTTagList endpoints = new NBTTagList();
//                tag.setTag("endpoints", endpoints);
//                for (ILDEndpoint endpoint : network.endpoints) {
//                    endpoints.appendTag(new NBTTagLong(endpoint.getPos().toLong()));
//                }
//            }
//            nbtTagCompound.setTag("nets", list);
//            return nbtTagCompound;
//        }
//
//        public World getWorld() {
//            return this.worldRef.get();
//        }
//    }
}
