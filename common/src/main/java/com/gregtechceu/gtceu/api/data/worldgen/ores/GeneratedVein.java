package com.gregtechceu.gtceu.api.data.worldgen.ores;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GeneratedVein {
    @Getter
    private final ChunkPos origin;

    private final Map<ChunkPos, Map<BlockPos, OreBlockPlacer>> generatedOres;
    private final Set<ChunkPos> unconsumedChunks;

    public GeneratedVein(ChunkPos origin, Map<BlockPos, OreBlockPlacer> oresByPosition) {
        this.origin = origin;
        this.generatedOres = oresByPosition.entrySet().stream().collect(Collectors.groupingBy(
                entry -> new ChunkPos(entry.getKey()),
                Object2ObjectOpenHashMap::new,
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, Object2ObjectOpenHashMap::new)
        ));

        this.unconsumedChunks = new ObjectArraySet<>(this.generatedOres.keySet());
    }

    public Map<BlockPos, OreBlockPlacer> consumeChunk(ChunkPos chunk) {
        var ores = this.generatedOres.get(chunk);

        if (ores == null)
            return Map.of();

        unconsumedChunks.remove(chunk);
        return ores;
    }

    public boolean isFullyConsumed() {
        return unconsumedChunks.isEmpty();
    }

    @Override
    public String toString() {
        return "GeneratedVein[origin=" + origin + ", chunks={" + generatedOres.keySet().stream().map(ChunkPos::toString).collect(Collectors.joining(", ")) + "}]";
    }
}
