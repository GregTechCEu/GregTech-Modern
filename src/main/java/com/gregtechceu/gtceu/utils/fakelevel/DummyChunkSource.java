package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class DummyChunkSource extends ChunkSource {

    @Getter
    private final SchemaLevel level;
    private final Long2ObjectMap<DummyChunk> chunks = new Long2ObjectOpenHashMap<>();
    @Getter
    private final LevelLightEngine lightEngine;

    public DummyChunkSource(SchemaLevel level) {
        this.level = level;
        this.lightEngine = new LevelLightEngine(this, true, true);
    }

    @Override
    public @Nullable ChunkAccess getChunk(int chunkX, int chunkZ, @NotNull ChunkStatus requiredStatus, boolean load) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        return chunks.computeIfAbsent(pos.toLong(), posLong1 -> {
            DummyChunk newChunk = new DummyChunk(level, pos);
            newChunk.setLoaded(true);
            return newChunk;
        });
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public void tick(@NotNull BooleanSupplier hasTimeLeft, boolean tickChunks) {}

    @Override
    public @NotNull String gatherStats() {
        return "Dummy";
    }

    @Override
    public int getLoadedChunksCount() {
        return chunks.size();
    }
}
