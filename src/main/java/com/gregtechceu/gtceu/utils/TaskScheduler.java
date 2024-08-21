package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.function.Task;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID)
public class TaskScheduler {

    @Nullable
    public static TaskScheduler get(Level world) {
        return tasksPerWorld.get(world);
    }

    private static final Map<Level, TaskScheduler> tasksPerWorld = new HashMap<>();

    private final List<Task> tasks = new ArrayList<>();
    private final List<Task> scheduledTasks = new ArrayList<>();
    private boolean running = false;

    public static void scheduleTask(Level world, Task task) {
        if (world.isClientSide) {
            throw new IllegalArgumentException("Attempt to schedule task on client world!");
        }
        tasksPerWorld.computeIfAbsent(world, k -> new TaskScheduler()).scheduleTask(task);
    }

    public void scheduleTask(Task task) {
        if (running) {
            scheduledTasks.add(task);
        } else {
            tasks.add(task);
        }
    }

    public void unload() {
        tasks.clear();
        scheduledTasks.clear();
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            tasksPerWorld.remove(event.getLevel());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide && event.phase == TickEvent.Phase.START) {
            TaskScheduler scheduler = get(event.level);
            if (scheduler != null) {
                if (!scheduler.scheduledTasks.isEmpty()) {
                    scheduler.tasks.addAll(scheduler.scheduledTasks);
                    scheduler.scheduledTasks.clear();
                }
                scheduler.running = true;
                scheduler.tasks.removeIf(task -> !task.run());
                scheduler.running = false;
            }
        }
    }

    public static Task weakTask(Task task) {
        return new Task() {

            private final WeakReference<Task> ref = new WeakReference<>(task);

            @Override
            public boolean run() {
                Task task = this.ref.get();
                if (task == null) return false;
                else return task.run();
            }
        };
    }
}
