package com.gregtechceu.gtceu.common.pipelike.cable;

import net.minecraft.world.level.Level;

import java.util.Arrays;

public class AveragingPerTickCounter {

    private final long defaultValue;
    private final long[] values;
    private long lastUpdatedWorldTime = 0;
    private int currentIndex = 0;
    private boolean dirty = true;
    private double lastAverage = 0;

    public AveragingPerTickCounter() {
        this(0, 20);
    }

    /**
     * Averages a value over a certain amount of ticks
     *
     * @param defaultValue self-explanatory
     * @param length       amount of ticks to average (20 for 1 second)
     */
    public AveragingPerTickCounter(long defaultValue, int length) {
        this.defaultValue = defaultValue;
        this.values = new long[length];
        Arrays.fill(values, defaultValue);
    }

    private void checkValueState(Level world) {
        if (world == null) return;
        long currentWorldTime = world.getGameTime();
        if (currentWorldTime != lastUpdatedWorldTime) {
            long dif = currentWorldTime - lastUpdatedWorldTime;
            if (dif >= values.length || dif < 0) {
                Arrays.fill(values, defaultValue);
                currentIndex = 0;
            } else {
                for (int i = currentIndex + 1; i <= currentIndex + dif; i++) {
                    values[i % values.length] = defaultValue;
                }
                currentIndex += dif;
                if (currentIndex >= values.length)
                    currentIndex = currentIndex % values.length;
            }
            this.lastUpdatedWorldTime = currentWorldTime;
            dirty = true;
        }
    }

    /**
     * @return the value from the current tick
     */
    public long getLast(Level world) {
        checkValueState(world);
        return values[currentIndex];
    }

    /**
     * @return the average of all values
     */
    public double getAverage(Level world) {
        checkValueState(world);
        if (!dirty)
            return lastAverage;
        dirty = false;
        return lastAverage = Arrays.stream(values).sum() / (double) (values.length);
    }

    /**
     * @param value the value to increment the current value by
     */
    public void increment(Level world, long value) {
        checkValueState(world);
        values[currentIndex] += value;
    }

    /**
     * @param value the value to set current value to
     */
    public void set(Level world, long value) {
        checkValueState(world);
        values[currentIndex] = value;
    }
}
