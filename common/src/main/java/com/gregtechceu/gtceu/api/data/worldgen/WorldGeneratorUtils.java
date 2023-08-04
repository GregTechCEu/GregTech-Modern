package com.gregtechceu.gtceu.api.data.worldgen;

import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.api.data.worldgen.vein.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.vein.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorldGeneratorUtils {
    public static final RuleTest END_ORE_REPLACEABLES = new TagMatchTest(CustomTags.ENDSTONE_ORE_REPLACEABLES);

    private static final Map<WorldGenLevel, WorldOreVeinCache> oreVeinCache = new WeakHashMap<>();

    public static final Map<String, IWorldGenLayer> WORLD_GEN_LAYERS = new HashMap<>();
    public static final Map<String, IStrataLayer> STRATA_LAYERS = new HashMap<>();
    public static final Map<BlockState, List<IStrataLayer>> STRATA_LAYER_BLOCK_MAP = new HashMap<>();
    public static final HashBiMap<ResourceLocation, Codec<? extends VeinGenerator>> VEIN_GENERATORS = HashBiMap.create();
    public static final HashBiMap<ResourceLocation, Function<GTOreFeatureEntry, ? extends VeinGenerator>> VEIN_GENERATOR_FUNCTIONS = HashBiMap.create();


    private static class WorldOreVeinCache {
        private final List<GTOreFeatureEntry> worldVeins;
        private final List<Entry<Integer, GTOreFeatureEntry>> veins = new LinkedList<>();

        public WorldOreVeinCache(WorldGenLevel level) {
            this.worldVeins = GTRegistries.ORE_VEINS.values().stream()
                    .filter(entry -> entry.getDimensionFilter().stream().anyMatch(filter -> filter.is(level.getLevel().dimensionTypeId())))
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
                    .map(vein -> new AbstractMap.SimpleEntry<>(vein.getWeight() + (vein.getBiomeWeightModifier() == null ? 0 : vein.getBiomeWeightModifier().apply(biome)), vein))
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

    public static DensityFunction.FunctionContext createFunctionContext(@Nullable WorldGenLevel level, int blockX, int blockY, int blockZ) {
        final Blender blender;
        if (level instanceof WorldGenRegion region) {
            blender = Blender.of(region);
        } else {
            blender = Blender.empty();
        }
        return new DensityFunction.FunctionContext()  {
            @Override
            public int blockX() {
                return blockX;
            }

            @Override
            public int blockY() {
                return blockY;
            }

            @Override
            public int blockZ() {
                return blockZ;
            }

            @Override
            public Blender getBlender() {
                return blender;
            }
        };
    }
}
