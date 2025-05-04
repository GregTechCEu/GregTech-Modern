package com.gregtechceu.gtceu.api.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.veins.*;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
@Accessors(chain = true, fluent = true)
public class OreVeinDefinition {

    // spotless:off
    public static final Codec<OreVeinDefinition> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("cluster_size").forGetter(OreVeinDefinition::clusterSize),
            Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(OreVeinDefinition::density),
            Codec.INT.fieldOf("weight").forGetter(OreVeinDefinition::weight),
            IWorldGenLayer.CODEC.fieldOf("layer").forGetter(OreVeinDefinition::layer),
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter").forGetter(ft -> new ArrayList<>(ft.dimensionFilter)),
            HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(OreVeinDefinition::heightRange),
            Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(OreVeinDefinition::discardChanceOnAirExposure),
            RegistryCodecs.homogeneousList(Registries.BIOME).lenientOptionalFieldOf("biomes", HolderSet.empty()).forGetter(OreVeinDefinition::biomes),
            BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", BiomeWeightModifier.EMPTY).forGetter(ext -> ext.biomeWeightModifier),
            VeinGenerator.DIRECT_CODEC.fieldOf("generator").forGetter(ft -> ft.veinGenerator),
            Codec.list(IndicatorGenerator.DIRECT_CODEC).fieldOf("indicators").forGetter(ft -> ft.indicatorGenerators)
    ).apply(instance, OreVeinDefinition::new));

    public static final Codec<Holder<OreVeinDefinition>> CODEC = RegistryFixedCodec.create(GTRegistries.ORE_VEIN_REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<OreVeinDefinition>> STREAM_CODEC = ByteBufCodecs.holderRegistry(GTRegistries.ORE_VEIN_REGISTRY);
    // spotless:on

    private final InferredProperties inferredProperties = new InferredProperties();

    @Getter
    private IntProvider clusterSize;
    @Getter
    private float density;
    @Getter
    private int weight;
    @Getter
    private IWorldGenLayer layer;
    @Getter
    @Setter
    private Set<ResourceKey<Level>> dimensionFilter;
    @Getter
    @Setter
    private HeightRangePlacement heightRange;
    @Getter
    @Setter
    private float discardChanceOnAirExposure;
    @Getter
    private HolderSet<Biome> biomes;
    @Getter
    @Setter
    private BiomeWeightModifier biomeWeightModifier;

    @Getter
    @Setter
    @Nullable
    private VeinGenerator veinGenerator;

    @Getter
    @Setter
    private List<IndicatorGenerator> indicatorGenerators;

    @ApiStatus.Internal
    @Nullable
    @Setter
    private HolderGetter<Biome> biomeLookup;

    public OreVeinDefinition(OreVeinDefinition other) {
        this(other.clusterSize, other.density, other.weight, other.layer,
                Set.copyOf(other.dimensionFilter), other.heightRange, other.discardChanceOnAirExposure,
                other.biomes, other.biomeWeightModifier, other.veinGenerator, List.copyOf(other.indicatorGenerators),
                other.biomeLookup);
    }

    public OreVeinDefinition(IntProvider clusterSize, float density, int weight, IWorldGenLayer layer,
                             List<ResourceKey<Level>> dimensionFilter, HeightRangePlacement heightRange,
                             float discardChanceOnAirExposure, HolderSet<Biome> biomes,
                             BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator,
                             @Nullable List<IndicatorGenerator> indicatorGenerators) {
        this(clusterSize, density, weight,
                layer, new HashSet<>(dimensionFilter), heightRange, discardChanceOnAirExposure, biomes,
                biomeWeightModifier,
                veinGenerator, indicatorGenerators, null);
    }

    public OreVeinDefinition(IntProvider clusterSize, float density, int weight, IWorldGenLayer layer,
                             Set<ResourceKey<Level>> dimensionFilter, HeightRangePlacement heightRange,
                             float discardChanceOnAirExposure, HolderSet<Biome> biomes,
                             BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator,
                             @Nullable List<IndicatorGenerator> indicatorGenerators,
                             @Nullable HolderGetter<Biome> biomeLookup) {
        this.clusterSize = clusterSize;
        this.density = density;
        this.weight = weight;
        this.layer = layer;
        this.dimensionFilter = dimensionFilter;
        this.heightRange = heightRange;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.biomes = biomes;
        this.biomeWeightModifier = biomeWeightModifier;
        this.veinGenerator = veinGenerator;
        this.indicatorGenerators = Objects.requireNonNullElseGet(indicatorGenerators, ArrayList::new);
        this.biomeLookup = biomeLookup;
    }

    public OreVeinDefinition clusterSize(IntProvider clusterSize) {
        this.clusterSize = clusterSize;
        return this;
    }

    public OreVeinDefinition clusterSize(int clusterSize) {
        this.clusterSize = ConstantInt.of(clusterSize);
        return this;
    }

    public OreVeinDefinition density(float density) {
        this.density = density;
        return this;
    }

    public OreVeinDefinition weight(int weight) {
        this.weight = weight;
        return this;
    }

    public OreVeinDefinition layer(IWorldGenLayer layer) {
        this.layer = layer;
        if (this.dimensionFilter == null || this.dimensionFilter.isEmpty()) {
            dimensions(layer.getLevels());
        }
        return this;
    }

    public OreVeinDefinition dimensions(Set<ResourceKey<Level>> dimensions) {
        this.dimensionFilter = dimensions;
        return this;
    }

    public OreVeinDefinition biomes(TagKey<Biome> biomes) {
        if (biomeLookup == null) {
            GTRegistries.builtinRegistry().registry(GTRegistries.ORE_VEIN_REGISTRY)
                    .map(reg -> reg.getKey(this))
                    .ifPresentOrElse(id -> {
                        GTCEu.LOGGER.error("Tried to modify ore vein `{}`'s biomes after registry has been frozen!",
                                id);
                    }, () -> {
                        GTCEu.LOGGER.error("Tried to modify an ore vein's biomes after registry has been frozen!");
                    });
            return this;
        }
        this.biomes = biomeLookup.getOrThrow(biomes);
        return this;
    }

    public OreVeinDefinition biomes(HolderSet<Biome> biomes) {
        this.biomes = Objects.requireNonNullElseGet(biomes, HolderSet::empty);
        return this;
    }

    public OreVeinDefinition heightRangeUniform(int min, int max) {
        heightRange(HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public OreVeinDefinition heightRangeTriangle(int min, int max) {
        heightRange(HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public OreVeinDefinition standardVeinGenerator(Consumer<StandardVeinGenerator> config) {
        var veinGenerator = new StandardVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinition layeredVeinGenerator(Consumer<LayeredVeinGenerator> config) {
        var veinGenerator = new LayeredVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinition geodeVeinGenerator(Consumer<GeodeVeinGenerator> config) {
        var veinGenerator = new GeodeVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinition dikeVeinGenerator(Consumer<DikeVeinGenerator> config) {
        var veinGenerator = new DikeVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinition veinedVeinGenerator(Consumer<VeinedVeinGenerator> config) {
        var veinGenerator = new VeinedVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinition classicVeinGenerator(Consumer<ClassicVeinGenerator> config) {
        var veinGenerator = new ClassicVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinition cuboidVeinGenerator(Consumer<CuboidVeinGenerator> config) {
        var veinGenerator = new CuboidVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minY(inferredProperties.heightRange.getFirst());
            veinGenerator.maxY(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    @Tolerate
    @Nullable
    public VeinGenerator veinGenerator(ResourceLocation id) {
        if (veinGenerator == null) {
            // noinspection DataFlowIssue
            veinGenerator = WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.containsKey(id) ?
                    WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.get(id).get() : null;
        }
        return veinGenerator;
    }

    public OreVeinDefinition surfaceIndicatorGenerator(Consumer<SurfaceIndicatorGenerator> config) {
        config.accept(getOrCreateIndicatorGenerator(SurfaceIndicatorGenerator.class, SurfaceIndicatorGenerator::new));
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends IndicatorGenerator> T getOrCreateIndicatorGenerator(Class<T> indicatorClass,
                                                                           Supplier<T> constructor) {
        var existingGenerator = indicatorGenerators.stream()
                .filter(indicatorClass::isInstance)
                .map(indicatorClass::cast)
                .findFirst().orElse(null);

        if (existingGenerator != null)
            return existingGenerator;

        var generator = constructor.get();
        indicatorGenerators.add(generator);
        return generator;
    }

    public boolean canGenerate() {
        if (this.veinGenerator() instanceof NoopVeinGenerator) {
            return false;
        }
        return this.weight() > 0 || !this.biomeWeightModifier().isEmpty();
    }

    private static class InferredProperties {

        public Pair<Integer, Integer> heightRange = null;
    }
}
