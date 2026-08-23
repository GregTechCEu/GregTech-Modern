package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.With;

@With
public record PredicateSettings(int priority,
                                int minCount, int maxCount,
                                int minSliceCount, int maxSliceCount,
                                int previewCount, boolean disableRenderFormed) {

    // spotless:off
    public static final Codec<PredicateSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("priority").forGetter(PredicateSettings::priority),
            Codec.INT.fieldOf("min_count").forGetter(PredicateSettings::minCount),
            Codec.INT.fieldOf("max_count").forGetter(PredicateSettings::maxCount),
            Codec.INT.fieldOf("min_layer_count").forGetter(PredicateSettings::minSliceCount),
            Codec.INT.fieldOf("max_layer_count").forGetter(PredicateSettings::maxSliceCount),
            Codec.INT.fieldOf("preview_count").forGetter(PredicateSettings::previewCount),
            Codec.BOOL.fieldOf("disable_render_formed").forGetter(PredicateSettings::disableRenderFormed)
    ).apply(instance, PredicateSettings::new));
    // spotless:on

    public PredicateSettings copy() {
        return new PredicateSettings(
                this.priority,
                this.minCount,
                this.maxCount,
                this.minSliceCount,
                this.maxSliceCount,
                this.previewCount,
                this.disableRenderFormed);
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

    /// simple test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        return testGlobalMin(ctx.getGlobalCount(this));
    }

    /// simple test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        return testSliceMin(ctx.getSliceCount(this));
    }

    /// simple test against global max count, increments global count
    public boolean testGlobalMax(PredicateContext ctx) {
        return testGlobalMax(ctx.incrementGlobalCount(this));
    }

    /// simple test against slice max count, increments slice count
    public boolean testSliceMax(PredicateContext ctx) {
        return testSliceMax(ctx.incrementSliceCount(this));
    }

    public static PredicateSettings create() {
        return new PredicateSettings(
                0,
                -1, -1,
                -1, -1,
                -1, false);
    }
}
