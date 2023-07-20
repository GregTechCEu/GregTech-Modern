package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskHandler {
    private static final Map<ResourceKey<Level>, List<RunnableEntry>> serverTasks = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<RunnableEntry>> waitToAddTasks = new HashMap<>();

    // schedule tick event here
    public static void onTickUpdate(Level level) {
        var key = level.dimension();
        execute(serverTasks.get(key));
        synchronized (waitToAddTasks) {
            serverTasks.putAll(waitToAddTasks);
            waitToAddTasks.clear();
        }
    }

    // clean up here
    public static void onWorldUnLoad(Level level) {
        var key = level.dimension();
        if (!level.isClientSide&& serverTasks.containsKey(key)) {
            serverTasks.remove(key);
            synchronized (waitToAddTasks) {
                waitToAddTasks.remove(key);
            }
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

    public static void enqueueServerTask(Level level, Runnable task, int delay) {
        if (!level.isClientSide) {
            synchronized (waitToAddTasks) {
                waitToAddTasks.computeIfAbsent(level.dimension(), key -> new ArrayList<>()).add(new RunnableEntry(task, delay));
            }
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
