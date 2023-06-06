package com.gregtechceu.gtceu.api.data.worldgen;

import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomeFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinCountFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOreFeatureEntry
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTOreFeatureEntry {
    public static final HashBiMap<ResourceLocation, GTOreFeatureEntry> ALL = HashBiMap.create();


    public static final Codec<GTOreFeatureEntry> CODEC = ResourceLocation.CODEC
            .flatXmap(rl -> Optional.ofNullable(ALL.get(rl))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("No GTOreFeatureEntry with id " + rl + " registered")),
                    obj -> Optional.ofNullable(ALL.inverse().get(obj))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("GTOreFeatureEntry " + obj + " not registered")));
    public static final Codec<GTOreFeatureEntry> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("cluster_size").forGetter(ft -> ft.clusterSize),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(ft -> ft.density),
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    IWorldGenLayer.CODEC.fieldOf("layer").forGetter(ft -> ft.layer),
                    RegistryCodecs.homogeneousList(Registry.DIMENSION_TYPE_REGISTRY).fieldOf("dimension_filter").forGetter(ft -> ft.dimensionFilter),
                    HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(ft -> ft.range),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(ft -> ft.discardChanceOnAirExposure),
                    RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(ext -> ext.biomes),
                    BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", null).forGetter(ext -> ext.biomeWeightModifier),
                    VeinGenerator.DIRECT_CODEC.fieldOf("generator").forGetter(ft -> ft.veinGenerator)
            ).apply(instance, GTOreFeatureEntry::new)
    );

    public final int clusterSize;
    public final float density;
    public final int weight;
    public final IWorldGenLayer layer;
    public final HolderSet<DimensionType> dimensionFilter;
    public final HeightRangePlacement range;
    public final float discardChanceOnAirExposure;
    public HolderSet<Biome> biomes;
    public BiomeWeightModifier biomeWeightModifier;

    public final List<PlacementModifier> modifiers;

    private VeinGenerator veinGenerator;

    public GTOreFeatureEntry(ResourceLocation id, int clusterSize, float density, int weight, IWorldGenLayer layer, HolderSet<DimensionType> dimensionFilter, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable HolderSet<Biome> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable GTOreFeatureEntry.VeinGenerator veinGenerator) {
        this(clusterSize, density, weight, layer, dimensionFilter, range, discardChanceOnAirExposure, biomes, biomeWeightModifier, veinGenerator);
        ALL.put(id, this);
    }

    public GTOreFeatureEntry(int clusterSize, float density, int weight, IWorldGenLayer layer, HolderSet<DimensionType> dimensionFilter, /*CountPlacement count,*/ HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable HolderSet<Biome> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable GTOreFeatureEntry.VeinGenerator veinGenerator) {
        this.clusterSize = clusterSize;
        this.density = density;
        this.weight = weight;
        this.layer = layer;
        this.dimensionFilter = dimensionFilter;
        this.range = range;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.biomes = biomes;
        this.biomeWeightModifier = biomeWeightModifier;
        this.veinGenerator = veinGenerator;

        this.modifiers = List.of(
                VeinCountFilter.count(),
                BiomeFilter.biome(),
                //this.count,
                InSquarePlacement.spread(),
                this.range
        );
    }

    public GTOreFeatureEntry biomes(TagKey<Biome> biomes) {
        this.biomes = BuiltinRegistries.BIOME.getOrCreateTag(biomes);
        return this;
    }

    public StandardVeinGenerator standardVeinGenerator() {
        if (this.veinGenerator == null) {
            this.veinGenerator = new StandardVeinGenerator(this);
        }
        return (StandardVeinGenerator) veinGenerator;
    }

    public LayeredVeinGenerator layeredVeinGenerator() {
        if (veinGenerator == null) {
            veinGenerator = new LayeredVeinGenerator(this);
        }
        return (LayeredVeinGenerator) veinGenerator;
    }

    @Nullable
    public GTOreFeatureEntry.VeinGenerator datagenExt() {
        return this.veinGenerator;
    }

    public static abstract class VeinGenerator {
        public static final Codec<Codec<? extends VeinGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC
                .flatXmap(rl -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.get(rl))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("No VeinGenerator with id " + rl + " registered")),
                        obj -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.inverse().get(obj))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("VeinGenerator " + obj + " not registered")));
        public static final Codec<VeinGenerator> DIRECT_CODEC = REGISTRY_CODEC.dispatchStable(VeinGenerator::codec, Function.identity());

        protected GTOreFeatureEntry entry;

        public VeinGenerator() {
        }

        public VeinGenerator(GTOreFeatureEntry entry) {
            this.entry = entry;
        }

        public ConfiguredFeature<?, ?> createConfiguredFeature() {
            build();
            GTOreFeatureConfiguration config = new GTOreFeatureConfiguration(entry);
            return new ConfiguredFeature<>(GTFeatures.ORE, config);
        }

        /*public PlacedFeature createPlacedFeature(RegistryAccess registryAccess) {
            Registry<ConfiguredFeature<?, ?>> featureRegistry = registryAccess.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
            Holder<ConfiguredFeature<?, ?>> featureHolder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTOreFeatureEntry.this.id));
            return new PlacedFeature(featureHolder, List.of(
                this.count,
                new FrequencyModifier(this.frequency),
                InSquarePlacement.spread()
                this.range
            ));
        }*/

        public abstract VeinGenerator build();

        public GTOreFeatureEntry parent() {
            return entry;
        }

        public abstract Codec<? extends VeinGenerator> codec();
    }

    public static class StandardVeinGenerator extends VeinGenerator {
        public static final Codec<StandardVeinGenerator> CODEC_SEPARATE = RecordCodecBuilder.create(instance -> instance.group(
                Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(ext -> ext.block.get()),
                Registry.BLOCK.byNameCodec().fieldOf("deep_block").forGetter(ext -> ext.deepBlock.get()),
                Registry.BLOCK.byNameCodec().fieldOf("nether_block").forGetter(ext -> ext.netherBlock.get())
        ).apply(instance, StandardVeinGenerator::new));
        public static final Codec<StandardVeinGenerator> CODEC_LIST = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("targets").forGetter(ext -> ext.blocks)
        ).apply(instance, StandardVeinGenerator::new));
        public static final Codec<StandardVeinGenerator> CODEC = Codec.either(CODEC_SEPARATE, CODEC_LIST).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

        public NonNullSupplier<? extends Block> block;
        public NonNullSupplier<? extends Block> deepBlock;
        public NonNullSupplier<? extends Block> netherBlock;

        public Either<List<OreConfiguration.TargetBlockState>, Material> blocks;

        public StandardVeinGenerator(GTOreFeatureEntry entry) {
            super(entry);
        }

        public StandardVeinGenerator(Block block, Block deepBlock, Block netherBlock) {
            this.block = NonNullSupplier.of(() -> block);
            this.deepBlock = NonNullSupplier.of(() -> deepBlock);
            this.netherBlock = NonNullSupplier.of(() -> netherBlock);
        }

        public StandardVeinGenerator(Either<List<OreConfiguration.TargetBlockState>, Material> blocks) {
            this.blocks = blocks;
        }

        public StandardVeinGenerator withBlock(NonNullSupplier<? extends Block> block) {
            this.block = block;
            this.deepBlock = block;
            return this;
        }

        public StandardVeinGenerator withNetherBlock(NonNullSupplier<? extends Block> block) {
            this.netherBlock = block;
            return this;
        }

        public StandardVeinGenerator withMaterial(Material material) {
            this.blocks = Either.right(material);
            return this;
        }

        public VeinGenerator build() {
            if (this.blocks != null) return this;
            // if (this.blocks.left().isPresent() && !this.blocks.left().get().isEmpty()) return this;
            List<OreConfiguration.TargetBlockState> targetStates = new ArrayList<>();
            if (this.block != null) {
                targetStates.add(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, this.block.get().defaultBlockState()));
            }

            if (this.deepBlock != null) {
                targetStates.add(OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, this.deepBlock.get().defaultBlockState()));
            }

            if (this.netherBlock != null) {
                targetStates.add(OreConfiguration.target(OreFeatures.NETHER_ORE_REPLACEABLES, this.netherBlock.get().defaultBlockState()));
            }

            this.blocks = Either.left(targetStates);
            return this;
        }

        @Override
        public Codec<? extends VeinGenerator> codec() {
            return CODEC;
        }
    }

    public static class LayeredVeinGenerator extends VeinGenerator {
        public static final Codec<LayeredVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        GTLayerPattern.CODEC.listOf().fieldOf("layer_patterns").forGetter(ft -> ft.layerPatterns != null ? ft.layerPatterns : ft.bakingLayerPatterns.stream().map(Supplier::get).collect(Collectors.toList()))
                ).apply(instance, LayeredVeinGenerator::new)
        );

        private final List<NonNullSupplier<GTLayerPattern>> bakingLayerPatterns = new ArrayList<>();

        public List<GTLayerPattern> layerPatterns;

        public LayeredVeinGenerator(GTOreFeatureEntry entry) {
            super(entry);
        }

        public LayeredVeinGenerator(List<GTLayerPattern> layerPatterns) {
            super();
            this.layerPatterns = layerPatterns;
        }

        public LayeredVeinGenerator withLayerPattern(NonNullSupplier<GTLayerPattern> pattern) {
            this.bakingLayerPatterns.add(pattern);
            return this;
        }

        public VeinGenerator build() {
            if (this.layerPatterns != null && !this.layerPatterns.isEmpty()) return this;
            List<GTLayerPattern> layerPatterns = this.bakingLayerPatterns.stream()
                    .map(NonNullSupplier::get)
                    .toList();
            this.layerPatterns = layerPatterns;
            return this;
        }

        @Override
        public Codec<? extends VeinGenerator> codec() {
            return CODEC;
        }
    }

}
