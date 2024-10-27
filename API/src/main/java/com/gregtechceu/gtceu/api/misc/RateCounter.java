package com.gregtechceu.gtceu.api.misc;

import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Arrays;
import java.util.function.LongSupplier;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Limits a rate/throughput across a certain amount of time, without needing an active tick subscription
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RateCounter {

    private final LongSupplier timeSupplier;

    private final long[] usedAmounts;
    private final long[] updateTimes;

    private final int countingTimeframe;

    public RateCounter(LongSupplier timeSupplier, int countingTimeframe) {
        this.timeSupplier = timeSupplier;
        this.countingTimeframe = countingTimeframe;

        this.usedAmounts = new long[countingTimeframe];
        this.updateTimes = new long[countingTimeframe];
    }

    public long getUsedSum() {
        long excludeUntilTime = timeSupplier.getAsLong() - countingTimeframe;
        long sum = 0L;

        for (int i = 0; i < countingTimeframe; i++) {
            if (updateTimes[i] <= excludeUntilTime) continue;
            sum += usedAmounts[i];
        }

        return sum;
    }

    public void addUsed(long amount) {
        long currentTime = timeSupplier.getAsLong();
        int idx = (int) (currentTime % countingTimeframe);

        if (updateTimes[idx] != currentTime)
            usedAmounts[idx] = 0L;

        usedAmounts[idx] += amount;
        updateTimes[idx] = currentTime;
    }

    public void clear() {
        Arrays.fill(usedAmounts, 0L);
    }

    public RateCounter copy() {
        var copied = new RateCounter(this.timeSupplier, this.countingTimeframe);
        System.arraycopy(this.usedAmounts, 0, copied.usedAmounts, 0, usedAmounts.length);
        System.arraycopy(this.updateTimes, 0, copied.updateTimes, 0, updateTimes.length);

        return copied;
    }
}
