package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class WorldGeneratorUtils {
    private static final Map<WorldGenLevel, WorldOreVeinCache> oreVeinCache = new WeakHashMap<>();


    private static class WorldOreVeinCache {
        private final List<GTOreFeatureEntry> worldVeins;
        private final List<Entry<Integer, GTOreFeatureEntry>> veins = new LinkedList<>();

        public WorldOreVeinCache(WorldGenLevel level) {
            this.worldVeins = GTOreFeatureEntry.ALL.values().stream()
                    .filter(entry -> entry.dimensionFilter.stream().anyMatch(filter -> filter.is(level.getLevel().dimensionTypeId())))
                    .collect(Collectors.toList());
        }

        private List<Entry<Integer, GTOreFeatureEntry>> getEntry(Holder<Biome> biome) {
            if (!veins.isEmpty())
                return veins;
            List<Entry<Integer, GTOreFeatureEntry>> result = worldVeins.stream()
                    /*.filter(entry -> {
                        HolderSet<Biome> checkingBiomes = entry.datagenExt().biomes.map(left -> left, right -> BuiltinRegistries.BIOME.getTag(right).orElse(null));
                        return checkingBiomes != null && checkingBiomes.contains(context);
                    })*/
                    .map(vein -> new AbstractMap.SimpleEntry<>(vein.weight + (vein.datagenExt().modifier == null ? 0 : vein.datagenExt().modifier.apply(biome)), vein))
                    .filter(entry -> entry.getKey() > 0)
                    .collect(Collectors.toList());
            veins.addAll(result);
            return result;
        }
    }

    public static List<Entry<Integer, GTOreFeatureEntry>> getCachedBiomeVeins(WorldGenLevel level, Holder<Biome> biome, RandomSource random) {
        if (oreVeinCache.containsKey(level))
            return oreVeinCache.get(level).getEntry(biome);
        WorldOreVeinCache worldOreVeinCache = new WorldOreVeinCache(level);
        oreVeinCache.put(level, worldOreVeinCache);
        return worldOreVeinCache.getEntry(biome);
    }
}
