package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.WeightedEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WorldGeneratorUtils {

    public static final RuleTest END_ORE_REPLACEABLES = new TagMatchTest(CustomTags.ENDSTONE_ORE_REPLACEABLES);

    private static final Map<ServerLevel, WorldOreVeinCache> oreVeinCache = new WeakHashMap<>();

    public static final SortedMap<String, IWorldGenLayer> WORLD_GEN_LAYERS = new Object2ObjectLinkedOpenHashMap<>();

    public static final HashBiMap<ResourceLocation, Codec<? extends VeinGenerator>> VEIN_GENERATORS = HashBiMap
            .create();
    public static final HashBiMap<ResourceLocation, Function<GTOreDefinition, ? extends VeinGenerator>> VEIN_GENERATOR_FUNCTIONS = HashBiMap
            .create();

    public static final HashBiMap<ResourceLocation, Codec<? extends IndicatorGenerator>> INDICATOR_GENERATORS = HashBiMap
            .create();
    public static final HashBiMap<ResourceLocation, Function<GTOreDefinition, ? extends IndicatorGenerator>> INDICATOR_GENERATOR_FUNCTIONS = HashBiMap
            .create();

    public record WeightedVein(GTOreDefinition vein, int weight) implements WeightedEntry {}

    private static class WorldOreVeinCache {

        private final List<GTOreDefinition> worldVeins;
        private final Map<Holder<Biome>, List<WeightedVein>> biomeVeins = new Object2ObjectOpenHashMap<>();

        public WorldOreVeinCache(ServerLevel level) {
            this.worldVeins = GTRegistries.ORE_VEINS.values().stream()
                    .filter(entry -> entry.dimensionFilter().stream()
                            .anyMatch(dim -> WorldGeneratorUtils.isSameDimension(dim, level.dimension())))
                    .collect(Collectors.toList());
        }

        private List<WeightedVein> getEntry(Holder<Biome> biome) {
            if (biomeVeins.containsKey(biome)) return biomeVeins.get(biome);
            var biomeVeins = worldVeins.stream()
                    .filter(vein -> vein.isForBiome(biome))
                    .map(vein -> new WeightedVein(vein, vein.weightForBiome(biome)))
                    .filter(vein -> vein.weight > 0)
                    .toList();
            this.biomeVeins.put(biome, biomeVeins);
            return biomeVeins;
        }
    }

    public static List<WeightedVein> getCachedBiomeVeins(ServerLevel level, Holder<Biome> biome) {
        return oreVeinCache.computeIfAbsent(level, WorldOreVeinCache::new).getEntry(biome);
    }

    public static Optional<String> getWorldGenLayerKey(IWorldGenLayer layer) {
        return WORLD_GEN_LAYERS.entrySet().stream()
                .filter(entry -> entry.getValue().equals(layer))
                .map(Entry::getKey)
                .findFirst();
    }

    public static boolean isSameDimension(ResourceKey<Level> first, ResourceKey<Level> second) {
        return first == second;
    }

    public static <T> Map<ChunkPos, Map<BlockPos, T>> groupByChunks(Map<BlockPos, T> input) {
        return input.entrySet().stream().collect(Collectors.groupingBy(
                entry -> new ChunkPos(entry.getKey()),
                Object2ObjectOpenHashMap::new,
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, Object2ObjectOpenHashMap::new)));
    }

    public static <T> Map<ChunkPos, List<BlockPos>> groupByChunks(Collection<BlockPos> positions) {
        return positions.stream().collect(Collectors.groupingBy(ChunkPos::new));
    }

    public static Collection<ChunkPos> getChunks(Collection<BlockPos> positions) {
        return positions.stream()
                .collect(Collectors.groupingBy(ChunkPos::new))
                .keySet();
    }

    public static void generateChunks(WorldGenLevel level, ChunkStatus requiredStatus, Collection<ChunkPos> chunks) {
        List<ChunkPos> previouslyUnloadedChunks = new ObjectArrayList<>();
        var chunkSource = level.getChunkSource();

        for (ChunkPos chunkPos : chunks) {
            var chunk = chunkSource.getChunk(chunkPos.x, chunkPos.z, false);

            if (chunk == null) {
                previouslyUnloadedChunks.add(chunkPos);
            }

            chunkSource.getChunk(chunkPos.x, chunkPos.z, requiredStatus, true);
        }

        if (level instanceof ServerLevel serverLevel) {
            previouslyUnloadedChunks.forEach(chunk -> serverLevel.unload(serverLevel.getChunk(chunk.x, chunk.z)));
        }
    }

    public static Optional<BlockPos> findBlockPos(BlockPos initialPos, Predicate<BlockPos> predicate,
                                                  Consumer<BlockPos.MutableBlockPos> step, int maxSteps) {
        var currentPos = initialPos.mutable();

        while (maxSteps-- >= 0) {
            step.accept(currentPos);

            if (predicate.test(currentPos))
                return Optional.of(currentPos.immutable());
        }

        return Optional.empty();
    }

    public static void invalidateOreVeinCache() {
        oreVeinCache.clear();
    }
}
