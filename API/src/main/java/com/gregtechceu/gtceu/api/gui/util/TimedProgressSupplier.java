package com.gregtechceu.gtceu.api.gui.util;

import java.util.function.DoubleSupplier;

public class TimedProgressSupplier implements DoubleSupplier {

    private final int msPerCycle;
    private final int maxValue;
    private final boolean countDown;
    private long startTime;

    public TimedProgressSupplier(int ticksPerCycle, int maxValue, boolean countDown) {
        this.msPerCycle = ticksPerCycle * 50;
        this.maxValue = maxValue;
        this.countDown = countDown;
        this.startTime = System.currentTimeMillis();
    }

    public void resetCountdown() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public double getAsDouble() {
        return calculateTime();
    }

    private double calculateTime() {
        long currentTime = System.currentTimeMillis();
        long msPassed = (currentTime - startTime) % msPerCycle;
        double currentValue = (1.0 * msPassed * maxValue) / msPerCycle;
        if (countDown) {
            return (maxValue - currentValue) / maxValue;
        }
        return currentValue / maxValue;
    }
}
