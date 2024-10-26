package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskHandler {

    private static final Map<ResourceKey<Level>, List<RunnableEntry>> serverTasks = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<RunnableEntry>> waitToAddTasks = new HashMap<>();

    // schedule tick event here
    public static void onTickUpdate(ServerLevel level) {
        var key = level.dimension();
        synchronized (waitToAddTasks) {
            var list = waitToAddTasks.remove(key);
            if (list != null && !list.isEmpty()) {
                serverTasks.computeIfAbsent(key, k -> new ArrayList<>()).addAll(list);
            }
        }
        execute(serverTasks.get(key));
    }

    // clean up here
    public static void onWorldUnLoad(ServerLevel level) {
        var key = level.dimension();
        serverTasks.remove(key);
        synchronized (waitToAddTasks) {
            waitToAddTasks.remove(key);
        }
    }

    private static void execute(List<RunnableEntry> tasks) {
        if (tasks == null || tasks.isEmpty()) return;
        var iter = tasks.iterator();
        while (iter.hasNext()) {
            var task = iter.next();
            if (task.delay <= 0) {
                try {
                    task.runnable.run();
                } catch (Exception e) {
                    GTCEu.LOGGER.error("error while schedule gregtech task", e);
                }
                iter.remove();
            } else {
                task.delay--;
            }
        }
    }

    public static void enqueueServerTask(ServerLevel level, Runnable task, int delay) {
        synchronized (waitToAddTasks) {
            waitToAddTasks.computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                    .add(new RunnableEntry(task, delay));
        }
    }

    private static class RunnableEntry {

        Runnable runnable;
        int delay;

        public RunnableEntry(Runnable runnable, int delay) {
            this.runnable = runnable;
            this.delay = delay;
        }
    }
}
