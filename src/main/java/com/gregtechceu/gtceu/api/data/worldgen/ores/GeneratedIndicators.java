package com.gregtechceu.gtceu.api.data.worldgen.ores;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.ChunkPos;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Holds a vein's {@link OreBlockPlacer}s for each of its blocks, grouped by chunk.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GeneratedIndicators {

    @Getter
    private final ChunkPos origin;

    private final Map<ChunkPos, List<OreIndicatorPlacer>> generatedIndicators;

    /**
     * @param origin     The vein's origin chunk (NOT its actual center, which may be outside the origin chunk)
     * @param indicators The ore placers for each ore block position.<br>
     *                   Doesn't need to be ordered, grouping by chunks is done internally.
     */
    public GeneratedIndicators(ChunkPos origin, Map<ChunkPos, List<OreIndicatorPlacer>> indicators) {
        this.origin = origin;

        this.generatedIndicators = indicators;
    }

    /**
     * Retrieve the indicator placers for the specified chunk.
     */
    public List<OreIndicatorPlacer> consumeIndicators(ChunkPos chunk) {
        return this.generatedIndicators.getOrDefault(chunk, List.of());
    }

    @Override
    public String toString() {
        return "GeneratedIndicators[origin=" + origin + ", chunks={" +
                generatedIndicators.keySet().stream().map(ChunkPos::toString).collect(Collectors.joining(", ")) + "}]";
    }
}
