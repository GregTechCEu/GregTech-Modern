package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

public class MultiblockWorldSavedData extends SavedData {

    public static MultiblockWorldSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(MultiblockWorldSavedData::new, MultiblockWorldSavedData::new, "gtceu_multiblock");
    }

    /**
     * Store all formed multiblocks' structure info
     */
    public final Map<BlockPos, Set<PatternState>> mapping;
    /**
     * Chunk pos mapping.
     */
    public final Map<ChunkPos, Set<PatternState>> chunkPosMapping;

    private MultiblockWorldSavedData() {
        this.mapping = new Object2ObjectOpenHashMap<>();
        this.chunkPosMapping = new HashMap<>();
    }

    private MultiblockWorldSavedData(CompoundTag tag) {
        this();
    }

    public PatternState[] getPatternsInChunk(ChunkPos chunkPos) {
        return chunkPosMapping.getOrDefault(chunkPos, Collections.emptySet()).toArray(PatternState[]::new);
    }

    public void addMapping(PatternState patternState) {
        this.mapping.computeIfAbsent(patternState.getControllerPos(), x -> new HashSet<>()).add(patternState);
        for (BlockPos blockPos : patternState.getPosCache()) {
            chunkPosMapping.computeIfAbsent(new ChunkPos(blockPos), c -> new HashSet<>()).add(patternState);
        }
    }

    public void removeMapping(PatternState patternState) {
        this.mapping.getOrDefault(patternState.getControllerPos(), new HashSet<>()).remove(patternState);
        this.mapping.entrySet().removeIf(e -> e.getValue().isEmpty());

        for (var patternSet : chunkPosMapping.values()) {
            patternSet.remove(patternState);
        }
        chunkPosMapping.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        return compound;
    }

    // ********************************* thread for searching ********************************* //
    private final CopyOnWriteArrayList<MultiblockControllerMachine> controllers = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService executorService;
    private final static ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("GTCEu Multiblock Async Thread-%d")
            .setDaemon(true)
            .build();
    private static final ThreadLocal<Boolean> IN_SERVICE = ThreadLocal.withInitial(() -> false);
    private long periodID = Long.MIN_VALUE;

    public void createExecutorService() {
        if (executorService != null && !executorService.isShutdown()) return;
        executorService = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
        executorService.scheduleAtFixedRate(this::searchingTask, 0, 250, TimeUnit.MILLISECONDS); // per 5 tick
    }

    /**
     * add a async logic runnable
     * 
     * @param controller controller
     */
    public void addAsyncLogic(MultiblockControllerMachine controller) {
        controllers.add(controller);
        createExecutorService();
    }

    /**
     * remove async controller
     * 
     * @param controller controller
     */
    public void removeAsyncLogic(MultiblockControllerMachine controller) {
        if (controllers.contains(controller)) {
            controllers.remove(controller);
            if (controllers.isEmpty()) {
                releaseExecutorService();
            }
        }
    }

    private void searchingTask() {
        try {
            if (!GTCEu.canGetServerLevel()) return;
            IN_SERVICE.set(true);
            for (var controller : controllers) {
                try {
                    controller.asyncCheckPattern(periodID);
                } catch (Throwable e) {
                    GTCEu.LOGGER.error("Error while assembling multiblock {}: {}", controller, e.getMessage());
                }
            }
        } catch (Throwable e) {
            GTCEu.LOGGER.error("Error while assembling multiblocks: {}", e.getMessage());
        } finally {
            IN_SERVICE.set(false);
        }
        periodID++;
    }

    public static boolean isThreadService() {
        return IN_SERVICE.get() && GTCEu.canGetServerLevel();
    }

    public void releaseExecutorService() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        executorService = null;
    }

    public long getPeriodID() {
        return periodID;
    }
}
