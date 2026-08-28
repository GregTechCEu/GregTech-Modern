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


    // helper for xor
    public boolean isNoneValid() {
        return minCount <= 0 && minSliceCount <= 0;
    }

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

    public static PredicateSettings create() {
        return new PredicateSettings(
                0,
                -1, -1,
                -1, -1,
                -1, false);
    }
}
