package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Holds a vein's {@link OreBlockPlacer}s for each of its blocks, grouped by chunk.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GeneratedVein {

    @Getter
    private final ChunkPos origin;

    @Getter
    private final IWorldGenLayer layer;

    private final Long2ObjectMap<OreBlockPlacer> oresBySection;
    private final Map<ChunkPos, OreIndicatorPlacer> generatedIndicators;

    /**
     * @param origin        The vein's origin chunk (NOT its actual center, which may be outside the origin chunk)
     * @param oresBySection The ore placers for each ore block position grouped by chunk.<br>
     *                      Doesn't need to be ordered.
     * @param indicators    The indicator placers for each chunk.
     */
    public GeneratedVein(ChunkPos origin, IWorldGenLayer layer,
                         Long2ObjectMap<OreBlockPlacer> oresBySection,
                         Map<ChunkPos, OreIndicatorPlacer> indicators) {
        this.origin = origin;
        this.layer = layer;

        this.oresBySection = oresBySection;
        this.generatedIndicators = indicators;
    }

    /**
     * Retrieve the ore placers for all blocks inside the specified chunk.
     */
    public @Nullable OreBlockPlacer consumeOres(long sectionPos) {
        return this.oresBySection.get(sectionPos);
    }

    /**
     * Retrieve the combined indicator placer for the specified chunk.
     */
    public @Nullable OreIndicatorPlacer consumeIndicators(ChunkPos chunk) {
        return this.generatedIndicators.get(chunk);
    }

    public LongSet getGeneratedSections() {
        return this.oresBySection.keySet();
    }

    @Override
    public String toString() {
        return "GeneratedVein[origin=" + origin + ", sections={" +
                oresBySection.keySet().longStream().mapToObj(SectionPos::of).map(SectionPos::toString)
                        .collect(Collectors.joining(", ")) +
                "}]";
    }
}
