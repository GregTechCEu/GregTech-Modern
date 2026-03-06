package com.gregtechceu.gtceu.integration.map.cache;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

public abstract class WorldCache {

    protected final Map<ResourceKey<Level>, DimensionCache> cache = new HashMap<>();

    public boolean addVein(ResourceKey<Level> dim, int gridX, int gridZ, GeneratedVeinMetadata vein) {
        if (!cache.containsKey(dim)) {
            cache.put(dim, new DimensionCache());
        }
        return cache.get(dim).addVein(gridX, gridZ, vein);
    }

    public List<GeneratedVeinMetadata> getNearbyVeins(ResourceKey<Level> dim, BlockPos pos, int blockRadius) {
        if (cache.containsKey(dim)) {
            return cache.get(dim).getNearbyVeins(pos, blockRadius);
        }
        return new ArrayList<>();
    }

    public List<GeneratedVeinMetadata> getVeinsInArea(ResourceKey<Level> dim, int[] bounds) {
        if (cache.containsKey(dim)) {
            return cache.get(dim).getVeinsInBounds(
                    new BlockPos(bounds[0], 0, bounds[1]),
                    new BlockPos(bounds[0] + bounds[2], 0, bounds[1] + bounds[3]));
        }
        return new ArrayList<>();
    }

    public void clear() {
        cache.clear();
    }

    public void oreVeinDefinitionsChanged(Registry<GTOreDefinition> registry) {
        // Existing instances of vein definitions referenced by the cache are now invalid. Repopulate them here.
        for (DimensionCache levelCache : cache.values()) {
            for (GridCache gridCache : levelCache.getCache().values()) {
                gridCache.getVeins().removeIf(vein -> {
                    Optional<Holder.Reference<GTOreDefinition>> def = registry.getHolder(vein.definition().getKey());
                    def.ifPresent(vein::definition);
                    return def.isEmpty();
                });
            }
        }
    }
}
