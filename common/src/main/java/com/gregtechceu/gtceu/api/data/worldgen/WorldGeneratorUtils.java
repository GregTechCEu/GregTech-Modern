package com.gregtechceu.gtceu.api.data.worldgen;

import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorldGeneratorUtils {
    public static final RuleTest END_ORE_REPLACEABLES = new TagMatchTest(CustomTags.ENDSTONE_ORE_REPLACEABLES);

    private static final Map<ServerLevel, WorldOreVeinCache> oreVeinCache = new WeakHashMap<>();

    public static final SortedMap<String, IWorldGenLayer> WORLD_GEN_LAYERS = new Object2ObjectLinkedOpenHashMap<>();

    public static final HashBiMap<ResourceLocation, Codec<? extends VeinGenerator>> VEIN_GENERATORS = HashBiMap.create();
    public static final HashBiMap<ResourceLocation, Function<GTOreDefinition, ? extends VeinGenerator>> VEIN_GENERATOR_FUNCTIONS = HashBiMap.create();

    public static final HashBiMap<ResourceLocation, Codec<? extends IndicatorGenerator>> INDICATOR_GENERATORS = HashBiMap.create();
    public static final HashBiMap<ResourceLocation, Function<GTOreDefinition, ? extends IndicatorGenerator>> INDICATOR_GENERATOR_FUNCTIONS = HashBiMap.create();

    private static class WorldOreVeinCache {
        private final List<GTOreDefinition> worldVeins;
        private final List<Entry<Integer, GTOreDefinition>> veins = new LinkedList<>();

        public WorldOreVeinCache(ServerLevel level) {
            this.worldVeins = GTRegistries.ORE_VEINS.values().stream()
                    .filter(entry -> entry.getDimensionFilter().stream().anyMatch(dim -> WorldGeneratorUtils.isSameDimension(dim, level.dimension())))
                    .collect(Collectors.toList());
        }

        private List<Entry<Integer, GTOreDefinition>> getEntry(Holder<Biome> biome) {
            if (!veins.isEmpty())
                return veins;
            List<Entry<Integer, GTOreDefinition>> result = worldVeins.stream()
                    .filter(entry -> entry.getBiomes() == null || entry.getBiomes().get().contains(biome))
                    .map(vein -> new AbstractMap.SimpleEntry<>(vein.getWeight() + (vein.getBiomeWeightModifier() == null ? 0 : vein.getBiomeWeightModifier().apply(biome)), vein))
                    .filter(entry -> entry.getKey() > 0)
                    .collect(Collectors.toList());
            veins.addAll(result);
            return result;
        }
    }

    public static List<Entry<Integer, GTOreDefinition>> getCachedBiomeVeins(ServerLevel level, Holder<Biome> biome, RandomSource random) {
        if (oreVeinCache.containsKey(level))
            return oreVeinCache.get(level).getEntry(biome);
        WorldOreVeinCache worldOreVeinCache = new WorldOreVeinCache(level);
        oreVeinCache.put(level, worldOreVeinCache);
        return worldOreVeinCache.getEntry(biome);
    }

    public static Optional<String> getWorldGenLayerKey(IWorldGenLayer layer) {
        return WORLD_GEN_LAYERS.entrySet().stream()
                .filter(entry -> entry.getValue().equals(layer))
                .map(Entry::getKey)
                .findFirst();
    }

    public static boolean isSameDimension(ResourceKey<Level> first, ResourceKey<Level> second) {
        return first.location().equals(second.location());
    }
}
