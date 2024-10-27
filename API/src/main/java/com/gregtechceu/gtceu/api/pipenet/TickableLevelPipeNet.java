package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public abstract class TickableLevelPipeNet<NodeDataType, T extends PipeNet<NodeDataType> & ITickablePipeNet>
                                          extends LevelPipeNet<NodeDataType, T> {

    private final Map<T, List<ChunkPos>> loadedChunksByPipeNet = new HashMap<>();
    private final Set<T> tickingPipeNets = new HashSet<>();
    private final Set<T> removeLater = new HashSet<>();

    public TickableLevelPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public TickableLevelPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    private boolean isChunkLoaded(ChunkPos chunkPos) {
        var worldServer = getWorld();
        if (worldServer == null) return false;
        return worldServer.getChunkSource().hasChunk(chunkPos.x, chunkPos.z);
    }

    protected abstract int getUpdateRate();

    public void update() {
        if (getWorld().getGameTime() % getUpdateRate() == 0L) {
            tickingPipeNets.forEach(ITickablePipeNet::update);
        }
        if (removeLater.size() > 0) {
            removeLater.forEach(tickingPipeNets::remove);
            removeLater.clear();
        }
    }

    public void onChunkLoaded(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        List<T> pipeNetsInThisChunk = this.pipeNetsByChunk.get(chunkPos);
        if (pipeNetsInThisChunk == null) return;
        for (T pipeNet : pipeNetsInThisChunk) {
            List<ChunkPos> loadedChunks = getOrCreateChunkListForPipeNet(pipeNet);
            if (loadedChunks.isEmpty()) {
                this.tickingPipeNets.add(pipeNet);
            }
            loadedChunks.add(chunkPos);
        }
    }

    public void onChunkUnloaded(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        List<T> pipeNetsInThisChunk = this.pipeNetsByChunk.get(chunkPos);
        if (pipeNetsInThisChunk == null) return;
        for (T pipeNet : pipeNetsInThisChunk) {
            List<ChunkPos> loadedChunks = this.loadedChunksByPipeNet.get(pipeNet);
            if (loadedChunks != null && loadedChunks.contains(chunkPos)) {
                loadedChunks.remove(chunkPos);
                if (loadedChunks.isEmpty()) {
                    removeFromTicking(pipeNet);
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        Map<T, List<ChunkPos>> pipeNetByLoadedChunks = pipeNets.stream()
                .map(pipeNet -> Pair.of(pipeNet, getPipeNetLoadedChunks(pipeNet)))
                .filter(pair -> !pair.getRight().isEmpty())
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        if (!pipeNetByLoadedChunks.isEmpty()) {
            this.tickingPipeNets.addAll(pipeNetByLoadedChunks.keySet());
            this.loadedChunksByPipeNet.putAll(pipeNetByLoadedChunks);
        }
    }

    @Override
    protected void addPipeNet(T pipeNet) {
        super.addPipeNet(pipeNet);
        List<ChunkPos> loadedChunks = getPipeNetLoadedChunks(pipeNet);
        if (!loadedChunks.isEmpty()) {
            this.loadedChunksByPipeNet.put(pipeNet, loadedChunks);
            this.tickingPipeNets.add(pipeNet);
        }
    }

    private List<ChunkPos> getPipeNetLoadedChunks(T pipeNet) {
        return pipeNet.getContainedChunks().stream()
                .filter(this::isChunkLoaded)
                .collect(Collectors.toList());
    }

    @Override
    protected void removePipeNet(T pipeNet) {
        super.removePipeNet(pipeNet);
        if (loadedChunksByPipeNet.containsKey(pipeNet)) {
            removeFromTicking(pipeNet);
        }
    }

    private void removeFromTicking(T pipeNet) {
        this.loadedChunksByPipeNet.remove(pipeNet);
        this.removeLater.add(pipeNet);
    }

    private List<ChunkPos> getOrCreateChunkListForPipeNet(T pipeNet) {
        return this.loadedChunksByPipeNet.computeIfAbsent(pipeNet, k -> new ArrayList<>());
    }

    @Override
    protected void addPipeNetToChunk(ChunkPos chunkPos, T pipeNet) {
        super.addPipeNetToChunk(chunkPos, pipeNet);
        if (isChunkLoaded(chunkPos)) {
            List<ChunkPos> loadedChunks = getOrCreateChunkListForPipeNet(pipeNet);
            if (loadedChunks.isEmpty()) {
                this.tickingPipeNets.add(pipeNet);
            }
            loadedChunks.add(chunkPos);
        }
    }

    @Override
    protected void removePipeNetFromChunk(ChunkPos chunkPos, T pipeNet) {
        super.removePipeNetFromChunk(chunkPos, pipeNet);
        List<ChunkPos> loadedChunks = this.loadedChunksByPipeNet.get(pipeNet);
        if (loadedChunks != null && loadedChunks.contains(chunkPos)) {
            loadedChunks.remove(chunkPos);
            if (loadedChunks.isEmpty()) {
                removeFromTicking(pipeNet);
            }
        }
    }
}
