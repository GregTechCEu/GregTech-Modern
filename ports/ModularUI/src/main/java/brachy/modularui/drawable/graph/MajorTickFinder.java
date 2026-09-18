package brachy.modularui.drawable.graph;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface MajorTickFinder {

    double[] find(double min, double max, double[] ticks);
}
