package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import com.lowdragmc.lowdraglib.Platform;

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

    private final ServerLevel serverLevel;

    public static MultiblockWorldSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new MultiblockWorldSavedData(serverLevel, tag),
                () -> new MultiblockWorldSavedData(serverLevel), "gtceu_multiblock");
    }

    /**
     * Store all formed multiblocks' structure info
     */
    public final Map<BlockPos, MultiblockState> mapping;
    /**
     * Chunk pos mapping.
     */
    public final Map<ChunkPos, Set<MultiblockState>> chunkPosMapping;

    private MultiblockWorldSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        this.mapping = new Object2ObjectOpenHashMap<>();
        this.chunkPosMapping = new HashMap<>();
    }

    private MultiblockWorldSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
    }

    public MultiblockState[] getControllerInChunk(ChunkPos chunkPos) {
        return chunkPosMapping.getOrDefault(chunkPos, Collections.emptySet()).toArray(MultiblockState[]::new);
    }

    public void addMapping(MultiblockState state) {
        this.mapping.put(state.controllerPos, state);
        for (BlockPos blockPos : state.getCache()) {
            chunkPosMapping.computeIfAbsent(new ChunkPos(blockPos), c -> new HashSet<>()).add(state);
        }
        setDirty(true);
    }

    public void removeMapping(MultiblockState state) {
        this.mapping.remove(state.controllerPos);
        for (Set<MultiblockState> set : chunkPosMapping.values()) {
            set.remove(state);
        }
        setDirty(true);
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        return compound;
    }

    // ********************************* thread for searching ********************************* //
    private final CopyOnWriteArrayList<IMultiController> controllers = new CopyOnWriteArrayList<>();
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
    public void addAsyncLogic(IMultiController controller) {
        controllers.add(controller);
        createExecutorService();
    }

    /**
     * remove async controller
     * 
     * @param controller controller
     */
    public void removeAsyncLogic(IMultiController controller) {
        if (controllers.contains(controller)) {
            controllers.remove(controller);
            if (controllers.isEmpty()) {
                releaseExecutorService();
            }
        }
    }

    private void searchingTask() {
        try {
            if (Platform.isServerNotSafe()) return;
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
        return IN_SERVICE.get() && !Platform.isServerNotSafe();
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
