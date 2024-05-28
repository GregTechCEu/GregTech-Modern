package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public abstract class LevelPipeNet<NodeDataType, T extends PipeNet<NodeDataType>> extends SavedData {

    private final ServerLevel serverLevel;
    protected List<T> pipeNets = new ArrayList<>();
    protected final Map<ChunkPos, List<T>> pipeNetsByChunk = new HashMap<>();

    public LevelPipeNet(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public LevelPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
        this.pipeNets = new ArrayList<>();
        ListTag allEnergyNets = tag.getList("PipeNets", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergyNets.size(); i++) {
            CompoundTag pNetTag = allEnergyNets.getCompound(i);
            T pipeNet = createNetInstance();
            pipeNet.deserializeNBT(pNetTag);
            addPipeNetSilently(pipeNet);
        }
        init();
    }

    public ServerLevel getWorld() {
        return serverLevel;
    }

    protected void init() {
        this.pipeNets.forEach(PipeNet::onNodeConnectionsUpdate);
    }

    public void addNode(BlockPos nodePos, NodeDataType nodeData, int mark, int openConnections, boolean isActive) {
        T myPipeNet = null;
        Node<NodeDataType> node = new Node<>(nodeData, openConnections, mark, isActive);
        for (Direction facing : GTUtil.DIRECTIONS) {
            BlockPos offsetPos = nodePos.relative(facing);
            T pipeNet = getNetFromPos(offsetPos);
            Node<NodeDataType> secondNode = pipeNet == null ? null : pipeNet.getAllNodes().get(offsetPos);
            if (pipeNet != null && pipeNet.canAttachNode(nodeData) &&
                    pipeNet.canNodesConnect(secondNode, facing.getOpposite(), node, null)) {
                if (myPipeNet == null) {
                    myPipeNet = pipeNet;
                    myPipeNet.addNode(nodePos, node);
                } else if (myPipeNet != pipeNet) {
                    myPipeNet.uniteNetworks(pipeNet);
                }
            }

        }
        if (myPipeNet == null) {
            myPipeNet = createNetInstance();
            myPipeNet.addNode(nodePos, node);
            addPipeNet(myPipeNet);
            setDirty();
        }
    }

    protected void addPipeNetToChunk(ChunkPos chunkPos, T pipeNet) {
        this.pipeNetsByChunk.computeIfAbsent(chunkPos, any -> new ArrayList<>()).add(pipeNet);
    }

    protected void removePipeNetFromChunk(ChunkPos chunkPos, T pipeNet) {
        List<T> list = this.pipeNetsByChunk.get(chunkPos);
        if (list != null) list.remove(pipeNet);
        if (list.isEmpty()) this.pipeNetsByChunk.remove(chunkPos);
    }

    public void removeNode(BlockPos nodePos) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.removeNode(nodePos);
        }
    }

    public void updateBlockedConnections(BlockPos nodePos, Direction side, boolean isBlocked) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateBlockedConnections(nodePos, side, isBlocked);
            pipeNet.onPipeConnectionsUpdate();
        }
    }

    public void updateData(BlockPos nodePos, NodeDataType data) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateNodeData(nodePos, data);
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateMark(nodePos, newMark);
        }
    }

    public T getNetFromPos(BlockPos blockPos) {
        List<T> pipeNetsInChunk = pipeNetsByChunk.getOrDefault(new ChunkPos(blockPos), Collections.emptyList());
        for (T pipeNet : pipeNetsInChunk) {
            if (pipeNet.containsNode(blockPos))
                return pipeNet;
        }
        return null;
    }

    protected void addPipeNet(T pipeNet) {
        addPipeNetSilently(pipeNet);
    }

    protected void addPipeNetSilently(T pipeNet) {
        this.pipeNets.add(pipeNet);
        pipeNet.getContainedChunks().forEach(chunkPos -> addPipeNetToChunk(chunkPos, pipeNet));
        pipeNet.isValid = true;
    }

    protected void removePipeNet(T pipeNet) {
        this.pipeNets.remove(pipeNet);
        pipeNet.getContainedChunks().forEach(chunkPos -> removePipeNetFromChunk(chunkPos, pipeNet));
        pipeNet.isValid = false;
        setDirty();
    }

    protected abstract T createNetInstance();

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag allPipeNets = new ListTag();
        for (T pipeNet : pipeNets) {
            CompoundTag pNetTag = pipeNet.serializeNBT();
            allPipeNets.add(pNetTag);
        }
        compound.put("PipeNets", allPipeNets);
        return compound;
    }
}
