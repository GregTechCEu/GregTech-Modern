package com.gregtechceu.gtceu.common.pipelike.net;

import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeActivableBlockEntity;
import com.gregtechceu.gtceu.utils.TaskScheduler;
import com.gregtechceu.gtceu.utils.function.Task;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class SlowActiveWalker implements Task {

    private static final int RECENT_WALKER_CUTOFF = 10;

    private static final Map<INetPath<?, ?>, Long> RECENT_DISPATCHES = new WeakHashMap<>();

    /**
     * Dispatches a slow walker along a path with default parameters.
     * 
     * @param world the world to schedule the task in. When this world is unloaded, the task will die no matter
     *              its state, so be careful!
     * @param path  the path to walk.
     * @param delay the ticks between steps of the walker
     */
    public static void dispatch(Level world, INetPath<? extends WorldPipeNetNode, ?> path, int delay) {
        dispatch(world, path, delay, 1, 1);
    }

    /**
     * Dispatches a slow walker along a path.
     * 
     * @param world        the world to schedule the task in. When this world is unloaded, the task will die no matter
     *                     its state, so be careful!
     * @param path         the path to walk.
     * @param delay        the ticks between steps of the walker
     * @param stepSize     the number of nodes within the path that the walker progresses every step
     * @param activeLength the number of tiles that will be left active behind a progressing walker
     */
    public static void dispatch(Level world, INetPath<? extends WorldPipeNetNode, ?> path, int delay,
                                int stepSize, int activeLength) {
        long tick = Platform.getMinecraftServer().getTickCount();
        RECENT_DISPATCHES.compute(path, (k, v) -> {
            if (v == null || v < tick) {
                SlowActiveWalker walker = new SlowActiveWalker(path, delay, stepSize, activeLength);
                TaskScheduler.scheduleTask(world, walker);
                return tick + RECENT_WALKER_CUTOFF;
            } else return v;
        });
    }

    private final INetPath<? extends WorldPipeNetNode, ?> path;
    private final int lastStep;
    private int index = 0;

    private final int delay;
    private final int stepSize;
    private final int activeLength;
    private int counter;

    protected SlowActiveWalker(INetPath<? extends WorldPipeNetNode, ?> path, int delay, int stepSize,
                               int activeLength) {
        this.path = path;
        this.delay = delay;
        this.stepSize = stepSize;
        this.activeLength = activeLength;
        this.lastStep = this.path.getOrderedNodes().size() + activeLength - 1;
        this.step(getSafe(-stepSize), getSafe(0));
    }

    @Override
    public boolean run() {
        counter++;
        if (counter >= delay) {
            counter = 0;
            for (int i = 0; i < stepSize; i++) {
                index++;
                this.step(getSafe(index - activeLength), getSafe(index));
                if (index >= lastStep) {
                    return false;
                }
            }
        }
        return true;
    }

    protected @Nullable WorldPipeNetNode getSafe(int index) {
        if (index >= path.getOrderedNodes().size()) return null;
        else if (index < 0) return null;
        else return path.getOrderedNodes().get(index);
    }

    protected void step(@Nullable WorldPipeNetNode previous, @Nullable WorldPipeNetNode next) {
        if (previous != null) activate(previous, false);
        if (next != null) activate(next, true);
    }

    protected void activate(@NotNull WorldPipeNetNode node, boolean active) {
        if (node.getBlockEntity() instanceof PipeActivableBlockEntity activable) {
            activable.setActive(active);
        }
    }
}
