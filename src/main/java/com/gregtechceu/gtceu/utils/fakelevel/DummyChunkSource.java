package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class DummyChunkSource extends ChunkSource {

    @Getter
    private final Level level;
    private final Long2ObjectMap<LevelChunk> loadedChunks = new Long2ObjectOpenHashMap<>();
    @Getter
    private final LevelLightEngine lightEngine;

    public DummyChunkSource(Level level) {
        this.level = level;
        this.lightEngine = new LevelLightEngine(this, true, false);
    }

    @Override
    public @Nullable ChunkAccess getChunk(int chunkX, int chunkZ, @NotNull ChunkStatus requiredStatus, boolean load) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        long asLong = pos.toLong();
        if (loadedChunks.containsKey(asLong) || !load) {
            return loadedChunks.get(asLong);
        }

        return loadedChunks.computeIfAbsent(asLong, posLong -> {
            LevelChunk newChunk = new LevelChunk(this.level, new ChunkPos(posLong));
            newChunk.setLoaded(true);
            newChunk.runPostLoad();
            newChunk.registerAllBlockEntitiesAfterLevelLoad();
            MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(newChunk, true));
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
        return loadedChunks.size();
    }
}
