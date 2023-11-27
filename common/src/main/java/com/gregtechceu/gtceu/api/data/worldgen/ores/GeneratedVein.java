package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


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

    private final Map<ChunkPos, Map<BlockPos, OreBlockPlacer>> generatedOres;
    private final Map<ChunkPos, Map<BlockPos, OreBlockPlacer>> generatedIndicators;

    /**
     * @param origin               The vein's origin chunk (NOT its actual center, which may be outside the origin chunk)
     * @param oresByPosition       The ore placers for each ore block position.<br>
     *                             Doesn't need to be ordered, grouping by chunks is done internally.
     * @param indicatorsByPosition The ore placers for each indicator block position.<br>
     *                             Doesn't need to be ordered, grouping by chunks is done internally.
     */
    public GeneratedVein(
            ChunkPos origin, IWorldGenLayer layer,
            Map<BlockPos, OreBlockPlacer> oresByPosition, Map<BlockPos, OreBlockPlacer> indicatorsByPosition
    ) {
        this.origin = origin;
        this.layer = layer;

        this.generatedOres = WorldGeneratorUtils.groupByChunks(oresByPosition);
        this.generatedIndicators = WorldGeneratorUtils.groupByChunks(indicatorsByPosition);
    }

    /**
     * Retrieve the ore placers for all blocks inside the specified chunk.
     * 
     * <p>This marks the chunk as consumed, allowing the vein to be deleted from the ore generation cache,
     * as soon as all of its chunks have been consumed.
     */
    public Map<BlockPos, OreBlockPlacer> consumeChunk(ChunkPos chunk) {
        Map<BlockPos, OreBlockPlacer> output = new Object2ObjectOpenHashMap<>();

        output.putAll(this.generatedIndicators.getOrDefault(chunk, Collections.emptyMap()));
        output.putAll(this.generatedOres.getOrDefault(chunk, Collections.emptyMap()));

        return output;
    }

    @Override
    public String toString() {
        return "GeneratedVein[origin=" + origin + ", chunks={" + generatedOres.keySet().stream().map(ChunkPos::toString).collect(Collectors.joining(", ")) + "}]";
    }
}
