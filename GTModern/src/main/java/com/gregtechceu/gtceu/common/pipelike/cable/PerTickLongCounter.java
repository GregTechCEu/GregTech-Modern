package com.gregtechceu.gtceu.common.pipelike.cable;

import net.minecraft.world.level.Level;

public class PerTickLongCounter {

    private final long defaultValue;
    private long lastUpdatedWorldTime;
    private long lastValue;
    private long currentValue;

    public PerTickLongCounter() {
        this(0);
    }

    public PerTickLongCounter(long defaultValue) {
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.lastValue = defaultValue;
    }

    private void checkValueState(Level world) {
        if (world == null) return;
        long currentWorldTime = world.getGameTime();
        if (currentWorldTime != lastUpdatedWorldTime) {
            if (currentWorldTime == lastUpdatedWorldTime + 1) {
                // last updated time is 1 tick ago, so we can move current value to last
                // before resetting it to default value
                this.lastValue = currentValue;
            } else {
                // otherwise, set last value as default value
                this.lastValue = defaultValue;
            }
            this.lastUpdatedWorldTime = currentWorldTime;
            this.currentValue = defaultValue;
        }
    }

    public long get(Level world) {
        checkValueState(world);
        return currentValue;
    }

    public long getLast(Level world) {
        checkValueState(world);
        return lastValue;
    }

    public void increment(Level world, long value) {
        checkValueState(world);
        this.currentValue += value;
    }

    public void set(Level world, long value) {
        checkValueState(world);
        this.currentValue = value;
    }
}
