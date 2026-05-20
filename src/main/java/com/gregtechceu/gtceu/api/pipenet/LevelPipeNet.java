package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LevelPipeNet extends SavedData {

    public static LevelPipeNet getLevelPipeNet(ServerLevel sLvl, PipeNetworkType type) {
        return sLvl.getDataStorage().computeIfAbsent(tag -> new LevelPipeNet(sLvl, type),
                () -> new LevelPipeNet(sLvl, type), "gt_pipenet_ " + type.networkID.getPath());
    }

    private final ServerLevel serverLevel;
    private final PipeNetworkType networkType;
    protected List<PipeNet> pipeNets = new ArrayList<>();
    protected final Map<ChunkPos, List<PipeNet>> pipeNetsByChunk = new HashMap<>();

    public LevelPipeNet(ServerLevel serverLevel, PipeNetworkType networkType) {
        this.serverLevel = serverLevel;
        this.networkType = networkType;
    }

    public ServerLevel getWorld() {
        return serverLevel;
    }

    protected void addPipeNetToChunk(ChunkPos chunkPos, PipeNet pipeNet) {
        this.pipeNetsByChunk.computeIfAbsent(chunkPos, any -> new ArrayList<>()).add(pipeNet);
    }

    protected void removePipeNetFromChunk(ChunkPos chunkPos, PipeNet pipeNet) {
        List<PipeNet> list = this.pipeNetsByChunk.get(chunkPos);
        if (list != null) list.remove(pipeNet);
        if (list != null && list.isEmpty()) this.pipeNetsByChunk.remove(chunkPos);
    }

    public <T extends PipeNet> T getNetFromPos(BlockPos blockPos) {
        return null;
    }

    public void addNode(BlockPos nodePos, PipeBlockEntity<?> pipe) {}

    public void removeNode(BlockPos nodePos) {}

    public void updateConnections(BlockPos nodePos, int connections, int blocked) {}

    public @Nullable PipeNode getNodeFromPos(BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        return compound;
    }
}
