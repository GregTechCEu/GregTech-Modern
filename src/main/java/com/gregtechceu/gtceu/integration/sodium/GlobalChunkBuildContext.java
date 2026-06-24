package com.gregtechceu.gtceu.integration.sodium;

import com.gregtechceu.gtceu.core.mixins.client.MinecraftAccessor;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;

public final class GlobalChunkBuildContext {
    private static ChunkBuildContext mainThreadContext;
    private static final Thread mainThread = ((MinecraftAccessor) Minecraft.getInstance()).gtceu$getGameThread();

    private GlobalChunkBuildContext() {}

    public static void setMainThread() {
        if (mainThread != Thread.currentThread()) {
            throw new IllegalStateException("Global chunk build context captured wrong thread");
        }
    }

    @Nullable
    public static ChunkBuildContext get() {
        Thread thread = Thread.currentThread();
        // Main thread first, because it's the most common case
        if (thread == mainThread) {
            return mainThreadContext;
        } else if (thread instanceof WorkerThread holder) {
            return holder.context;
        } else {
            return null;
        }
    }

    public static void bindMainThread(ChunkBuildContext context) {
        mainThreadContext = context;
    }

    public static class WorkerThread extends Thread {
        private final ChunkBuildContext context;

        public WorkerThread(Runnable runnable, String name, ChunkBuildContext context) {
            super(runnable, name);
            this.context = context;
        }
    }
}
