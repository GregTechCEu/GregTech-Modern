package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomeFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.DimensionFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.FrequencyModifier;
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
    public static final Map<ResourceLocation, GTOreFeatureEntry> ALL = new HashMap<>();


    public static final Codec<GTOreFeatureEntry> CODEC = ResourceLocation.CODEC.comapFlatMap(GTOreFeatureEntry::read, (entry) -> entry.id);
    public static final Codec<GTOreFeatureEntry> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(ft -> ft.id),
                    Codec.INT.fieldOf("cluster_size").forGetter(ft -> ft.clusterSize),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(ft -> ft.density),
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    RegistryCodecs.homogeneousList(Registry.DIMENSION_TYPE_REGISTRY).fieldOf("dimension_filter").forGetter(ft -> ft.dimensionFilter),
                    CountPlacement.CODEC.fieldOf("count").forGetter(ft -> ft.count),
                    HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(ft -> ft.range),
                    //DimensionFilter.CODEC.fieldOf("dimension_filter").forGetter(ft -> ft.dimensionFilter),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(ft -> ft.discardChanceOnAirExposure),
                    Codec.either(StandardDatagenExtension.CODEC, LayeredDatagenExtension.CODEC)
                            .xmap(either ->
                                            either.map(Function.identity(), Function.identity()),
                                    result -> result instanceof StandardDatagenExtension standard ? Either.left(standard) : Either.right((LayeredDatagenExtension) result)
                            ).fieldOf("generator").forGetter(ft -> ft.datagenExt)
            ).apply(instance, GTOreFeatureEntry::new)
    );

    public final ResourceLocation id;
    public final int clusterSize;
    public final float density;
    public final int weight;
    public final HolderSet<DimensionType> dimensionFilter;
    public final CountPlacement count;
    public final HeightRangePlacement range;
    public final float discardChanceOnAirExposure;

    public final List<PlacementModifier> modifiers;

    private DatagenExtension datagenExt;

    public GTOreFeatureEntry(ResourceLocation id, int clusterSize, float density, int weight, HolderSet<DimensionType> dimensionFilter, CountPlacement count, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable DatagenExtension datagenExt) {
        this.id = id;
        this.clusterSize = clusterSize;
        this.density = density;
        this.weight = weight;
        this.dimensionFilter = dimensionFilter;
        this.count = count;
        this.range = range;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.datagenExt = datagenExt;

        this.modifiers = List.of(
                //new DimensionFilter(dimensionFilter),
                BiomeFilter.biome(),
                this.count,
                RarityFilter.onAverageOnceEvery(8),
                InSquarePlacement.spread(),
                this.range
        );
        ALL.put(id, this);
    }

    public StandardDatagenExtension standardDatagenExt() {
        if (this.datagenExt == null) {
            this.datagenExt = new GTOreFeatureEntry.StandardDatagenExtension(this);
        }
        return (StandardDatagenExtension) datagenExt;
    }

    public LayeredDatagenExtension layeredDatagenExt() {
        if (datagenExt == null) {
            datagenExt = new LayeredDatagenExtension(this);
        }
        return (LayeredDatagenExtension) datagenExt;
    }

    @Nullable
    public DatagenExtension datagenExt() {
        return this.datagenExt != null ? this.datagenExt : null;
    }

    public String getName() {
        return this.id.getPath();
    }

    public static DataResult<GTOreFeatureEntry> read(ResourceLocation id) {
        GTOreFeatureEntry entry = ALL.get(id);
        return entry != null ? DataResult.success(entry) : DataResult.error("Not a valid GTOreFeature: " + id);
    }

    public static abstract class DatagenExtension {
        public Either<HolderSet<Biome>, TagKey<Biome>> biomes;
        public BiomeWeightModifier modifier;

        protected GTOreFeatureEntry entry;

        public static <S extends DatagenExtension> RecordCodecBuilder<S, HolderSet<Biome>> biomesCodec(RecordCodecBuilder.Instance<S> instance) {
            return RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(ext -> ext.biomes.left().get());
        }

        public static <S extends DatagenExtension> RecordCodecBuilder<S, BiomeWeightModifier> modifierCodec(RecordCodecBuilder.Instance<S> instance) {
            return BiomeWeightModifier.CODEC.fieldOf("weight_modifier").forGetter(ext -> ext.modifier);
        }

        public DatagenExtension() {

        }

        public DatagenExtension(HolderSet<Biome> biomes, BiomeWeightModifier modifier) {
            this.biomes = Either.left(biomes);
            this.modifier = modifier;
        }

        public DatagenExtension(GTOreFeatureEntry entry) {
            this.entry = entry;
        }

        public GTOreFeatureEntry.DatagenExtension biomes(TagKey<Biome> biomes) {
            this.biomes = Either.right(biomes);
            return this;
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

        public abstract DatagenExtension build();

        public GTOreFeatureEntry parent() {
            return entry;
        }

        public abstract Codec<? extends DatagenExtension> codec();
    }

    public static class StandardDatagenExtension extends GTOreFeatureEntry.DatagenExtension {
        public static final Codec<StandardDatagenExtension> CODEC_SEPARATE = RecordCodecBuilder.create(instance -> instance.group(
                DatagenExtension.biomesCodec(instance),
                DatagenExtension.modifierCodec(instance),
                Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(ext -> ext.block.get()),
                Registry.BLOCK.byNameCodec().fieldOf("deep_block").forGetter(ext -> ext.deepBlock.get()),
                Registry.BLOCK.byNameCodec().fieldOf("nether_block").forGetter(ext -> ext.netherBlock.get())
        ).apply(instance, StandardDatagenExtension::new));
        public static final Codec<StandardDatagenExtension> CODEC_LIST = RecordCodecBuilder.create(instance -> instance.group(
                OreConfiguration.TargetBlockState.CODEC.listOf().fieldOf("targets").forGetter(ext -> ext.blocks)
        ).apply(instance, StandardDatagenExtension::new));
        public static final Codec<StandardDatagenExtension> CODEC = Codec.either(CODEC_SEPARATE, CODEC_LIST).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

        public NonNullSupplier<? extends Block> block;
        public NonNullSupplier<? extends Block> deepBlock;
        public NonNullSupplier<? extends Block> netherBlock;

        public List<OreConfiguration.TargetBlockState> blocks;

        public StandardDatagenExtension(GTOreFeatureEntry entry) {
            super(entry);
        }

        public StandardDatagenExtension(HolderSet<Biome> biomes, BiomeWeightModifier modifier, Block block, Block deepBlock, Block netherBlock) {
            super(biomes, modifier);
            this.block = NonNullSupplier.of(() -> block);
            this.deepBlock = NonNullSupplier.of(() -> deepBlock);
            this.netherBlock = NonNullSupplier.of(() -> netherBlock);
        }

        public StandardDatagenExtension(List<OreConfiguration.TargetBlockState> blocks) {
            this.blocks = blocks;
        }

        public GTOreFeatureEntry.StandardDatagenExtension withBlock(NonNullSupplier<? extends Block> block) {
            this.block = block;
            this.deepBlock = block;
            return this;
        }

        public GTOreFeatureEntry.StandardDatagenExtension withNetherBlock(NonNullSupplier<? extends Block> block) {
            this.netherBlock = block;
            return this;
        }

        public GTOreFeatureEntry.StandardDatagenExtension biomes(TagKey<Biome> biomes) {
            super.biomes(biomes);
            return this;
        }

        public DatagenExtension build() {
            if (this.blocks != null && !this.blocks.isEmpty()) return this;
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

            this.blocks = targetStates;
            return this;
        }

        @Override
        public Codec<? extends DatagenExtension> codec() {
            return CODEC;
        }
    }

    public static class LayeredDatagenExtension extends DatagenExtension {
        public static final Codec<LayeredDatagenExtension> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        DatagenExtension.biomesCodec(instance),
                        DatagenExtension.modifierCodec(instance),
                        GTLayerPattern.CODEC.listOf().fieldOf("layer_patterns").forGetter(ft -> ft.bakingLayerPatterns.stream().map(Supplier::get).collect(Collectors.toList()))
                ).apply(instance, LayeredDatagenExtension::new)
        );

        private final List<NonNullSupplier<GTLayerPattern>> bakingLayerPatterns = new ArrayList<>();

        public List<GTLayerPattern> layerPatterns;

        public LayeredDatagenExtension(GTOreFeatureEntry entry) {
            super(entry);
        }

        public LayeredDatagenExtension(HolderSet<Biome> biomes, BiomeWeightModifier modifier, List<GTLayerPattern> bakingLayerPatterns) {
            super(biomes, modifier);
            this.bakingLayerPatterns.addAll(bakingLayerPatterns.stream().map(val -> NonNullSupplier.of(() -> val)).toList());
        }

        public LayeredDatagenExtension withLayerPattern(NonNullSupplier<GTLayerPattern> pattern) {
            this.bakingLayerPatterns.add(pattern);
            return this;
        }

        @Override
        public LayeredDatagenExtension biomes(TagKey<Biome> biomes) {
            super.biomes(biomes);
            return this;
        }

        public DatagenExtension build() {
            List<GTLayerPattern> layerPatterns = this.bakingLayerPatterns.stream()
                    .map(NonNullSupplier::get)
                    .toList();
            this.layerPatterns = layerPatterns;
            return this;
        }

        @Override
        public Codec<? extends DatagenExtension> codec() {
            return CODEC;
        }
    }

}
