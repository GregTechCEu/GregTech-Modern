package com.gregtechceu.gtceu.api.mui.drawable.graph;

import lombok.Getter;
import lombok.Setter;

public class AutoMajorTickFinder implements MajorTickFinder {

    @Getter
    private final boolean autoAdjust;
    @Setter
    private double multiple = 10;

    public AutoMajorTickFinder(boolean autoAdjust) {
        this.autoAdjust = autoAdjust;
    }

    public AutoMajorTickFinder(double multiple) {
        autoAdjust = false;
        this.multiple = multiple;
    }

    @Override
    public double[] find(double min, double max, double[] ticks) {
        int s = (int) Math.ceil((max - min) / multiple) + 2;
        if (s > ticks.length) ticks = new double[s];
        double next = (Math.floor(min / multiple) * multiple);
        for (int i = 0; i < s; i++) {
            ticks[i] = next;
            if (next > max) {
                s = i + 1;
                break;
            }
            next += multiple;
        }
        if (ticks.length > s) ticks[s] = Float.NaN;
        return ticks;
    }

    void calculateAutoTickMultiple(double min, double max) {
        double step = (max - min) / 5;
        if (step < 1) {
            int significantPlaces = (int) Math.abs(Math.log10(step)) + 2;
            double ten = Math.pow(10, significantPlaces);
            step = (int) (step * ten + 0.2f) / ten;
        } else if (step == 1) {
            step = 0.2f;
        } else {
            int significantPlaces = (int) Math.log10(step) - 1;
            double ten = Math.pow(10, significantPlaces);
            step = (int) (step / ten + 0.2f) * ten;
        }
        setMultiple(step);
    }
}
