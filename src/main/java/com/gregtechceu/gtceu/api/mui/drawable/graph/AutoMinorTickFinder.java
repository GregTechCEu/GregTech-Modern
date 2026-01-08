package com.gregtechceu.gtceu.api.mui.drawable.graph;

public class AutoMinorTickFinder implements MinorTickFinder {

    private int amountBetweenMajors;

    public AutoMinorTickFinder(int amountBetweenMajors) {
        this.amountBetweenMajors = amountBetweenMajors;
    }

    @Override
    public double[] find(double min, double max, double[] majorTicks, double[] ticks) {
        int s = majorTicks.length * this.amountBetweenMajors;
        if (ticks.length < s) ticks = new double[s];
        int k = 0;
        for (int i = 0; i < majorTicks.length - 1; i++) {
            if (Double.isNaN(majorTicks[i + 1])) break;
            double next = majorTicks[i];
            double d = (majorTicks[i + 1] - next) / (amountBetweenMajors + 1);
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
