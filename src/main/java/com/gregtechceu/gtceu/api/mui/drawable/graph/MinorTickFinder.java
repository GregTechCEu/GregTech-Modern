package com.gregtechceu.gtceu.api.mui.drawable.graph;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface MinorTickFinder {

    double[] find(double min, double max, double[] majorTicks, double[] ticks);
}
