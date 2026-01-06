package com.gregtechceu.gtceu.api.mui.drawable.graph;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;

public class AutoMajorTickFinder implements MajorTickFinder {

    private float multiple;

    public AutoMajorTickFinder(float multiple) {
        this.multiple = multiple;
    }

    @Override
    public float[] find(float min, float max, float[] ticks) {
        int s = (int) Math.ceil((max - min) / multiple) + 1;
        if (s > ticks.length) ticks = new float[s];
        float next = (float) (Math.floor(min / multiple) * multiple);
        for (int i = 0; i < s; i++) {
            if (next > max) {
                s--;
                break;
            }
            ticks[i] = next;
            next += multiple;
        }
        if (ticks.length > s) ticks[s] = Float.NaN;
        return ticks;
    }
}
