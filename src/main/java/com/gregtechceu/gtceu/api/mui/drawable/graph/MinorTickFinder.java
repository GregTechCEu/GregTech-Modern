package com.gregtechceu.gtceu.api.mui.drawable.graph;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface MinorTickFinder {

    float[] find(float min, float max, float[] majorTicks, float[] ticks);
}
