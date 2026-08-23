package com.gregtechceu.gtceu.api.multiblock.predicates;

import lombok.With;

@With
public record PredicateSettings(int priority,
                                int minCount, int maxCount,
                                int minSliceCount, int maxSliceCount,
                                int previewCount, boolean disableRenderFormed) {

    public PredicateSettings copy() {
        return new PredicateSettings(
                this.priority,
                this.minCount,
                this.maxCount,
                this.minSliceCount,
                this.maxSliceCount,
                this.previewCount,
                this.disableRenderFormed
        );
    }

    public int comparePriority(PredicateSettings other) {
        return Integer.compare(this.priority, other.priority);
    }

    /// simple test against global min count
    public boolean testGlobalMin(int count) {
        return minCount == -1 || count >= minCount;
    }

    /// simple test against slice min count
    public boolean testSliceMin(int count) {
        return minSliceCount == -1 || count >= minSliceCount;
    }

    /// simple test against global max count
    public boolean testGlobalMax(int count) {
        return maxCount == -1 || count <= maxCount;
    }

    /// simple test against slice max count
    public boolean testSliceMax(int count) {
        return maxSliceCount == -1 || count <= maxSliceCount;
    }

    public static PredicateSettings create() {
        return new PredicateSettings(
                0,
                -1, -1,
                -1, -1,
                -1, false
        );
    }
}
