package com.gregtechceu.gtceu.api.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiblockWorldSavedData extends SavedData {

    public static MultiblockWorldSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(MultiblockWorldSavedData::new, MultiblockWorldSavedData::new, "gtceu_multiblock");
    }

    /**
     * Chunk pos mapping.
     */
    public final Map<ChunkPos, Set<MultiblockState>> chunkPosMapping;

    private MultiblockWorldSavedData() {
        this.chunkPosMapping = new HashMap<>();
    }

    private MultiblockWorldSavedData(CompoundTag tag) {
        this();
    }

    public Set<MultiblockState> getControllersInChunk(ChunkPos chunkPos) {
        return chunkPosMapping.getOrDefault(chunkPos, Collections.emptySet());
    }

    public void addMapping(MultiblockState state) {
        for (BlockPos blockPos : state.getCache()) {
            chunkPosMapping.computeIfAbsent(new ChunkPos(blockPos), c -> new HashSet<>()).add(state);
        }
    }

    public void removeMapping(MultiblockState state) {
        for (Set<MultiblockState> set : chunkPosMapping.values()) {
            set.remove(state);
        }
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        return compound;
    }
}
