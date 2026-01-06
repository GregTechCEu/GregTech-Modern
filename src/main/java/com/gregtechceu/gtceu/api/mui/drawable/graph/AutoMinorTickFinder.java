package com.gregtechceu.gtceu.api.mui.drawable.graph;

public class AutoMinorTickFinder implements MinorTickFinder {

    private int amountBetweenMajors;

    public AutoMinorTickFinder(int amountBetweenMajors) {
        this.amountBetweenMajors = amountBetweenMajors;
    }

    @Override
    public float[] find(float min, float max, float[] majorTicks, float[] ticks) {
        int s = majorTicks.length * this.amountBetweenMajors;
        if (ticks.length < s) ticks = new float[s];
        int k = 0;
        for (int i = 0; i < majorTicks.length - 1; i++) {
            if (Float.isNaN(majorTicks[i + 1])) break;
            float next = majorTicks[i];
            float d = (majorTicks[i + 1] - next) / (amountBetweenMajors + 1);
            for (int j = 0; j < amountBetweenMajors; j++) {
                next += d;
                if (next >= min) ticks[k++] = next;
                if (next > max) {
                    ticks[k] = Float.NaN;
                    break;
                }
            }
        }
        if (k < ticks.length) ticks[k] = Float.NaN;
        return ticks;
    }
}
