package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.*;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Screret
 * @date 2023/6/14
 * @implNote GTOreDefinition
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(chain = true, fluent = true)
public class GTOreDefinition {
    public static final Codec<GTOreDefinition> CODEC = ResourceLocation.CODEC
            .flatXmap(rl -> Optional.ofNullable(GTRegistries.ORE_VEINS.get(rl))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "No GTOreDefinition with id " + rl + " registered")),
                    obj -> Optional.ofNullable(GTRegistries.ORE_VEINS.getKey(obj))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "GTOreDefinition " + obj + " not registered")));
    public static final Codec<GTOreDefinition> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("cluster_size").forGetter(ft -> ft.clusterSize),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(ft -> ft.density),
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    IWorldGenLayer.CODEC.fieldOf("layer").forGetter(ft -> ft.layer),
                    ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter").forGetter(ft -> new ArrayList<>(ft.dimensionFilter)),
                    HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(ft -> ft.range),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(ft -> ft.discardChanceOnAirExposure),
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes", null).forGetter(ext -> ext.biomes == null ? null : ext.biomes.get()),
                    BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", null).forGetter(ext -> ext.biomeWeightModifier),
                    VeinGenerator.DIRECT_CODEC.fieldOf("generator").forGetter(ft -> ft.veinGenerator),
                    Codec.list(IndicatorGenerator.DIRECT_CODEC).fieldOf("indicators").forGetter(ft -> ft.indicatorGenerators)
            ).apply(instance, (clusterSize, density, weight, layer, dimensionFilter, range, discardChanceOnAirExposure, biomes, biomeWeightModifier, veinGenerator, indicatorGenerators) ->
                    new GTOreDefinition(clusterSize, density, weight, layer, new HashSet<>(dimensionFilter), range, discardChanceOnAirExposure, biomes == null ? null : () -> biomes, biomeWeightModifier, veinGenerator, indicatorGenerators))
    );

    private final InferredProperties inferredProperties = new InferredProperties();

    @Getter
    private int clusterSize;
    @Getter
    private float density;
    @Getter
    private int weight;
    @Getter @Setter
    private IWorldGenLayer layer;
    @Getter @Setter
    private Set<ResourceKey<Level>> dimensionFilter;
    @Getter @Setter
    private HeightRangePlacement range;
    @Getter @Setter
    private float discardChanceOnAirExposure;
    @Getter @Setter
    private Supplier<HolderSet<Biome>> biomes;
    @Getter @Setter
    private BiomeWeightModifier biomeWeightModifier;

    @Getter @Setter
    private VeinGenerator veinGenerator;

    @Getter @Setter
    private List<IndicatorGenerator> indicatorGenerators;

    @Getter @Setter
    private int minimumYield, maximumYield, depletedYield, depletionChance, depletionAmount = 1;
    @Setter
    private List<Map.Entry<Integer, Material>> bedrockVeinMaterial;

    public GTOreDefinition(GTOreDefinition other) {
        this(
                other.clusterSize, other.density, other.weight, other.layer,
                Set.copyOf(other.dimensionFilter), other.range, other.discardChanceOnAirExposure,
                other.biomes, other.biomeWeightModifier, other.veinGenerator, List.copyOf(other.indicatorGenerators)
        );
    }

    public GTOreDefinition(int clusterSize, float density, int weight, IWorldGenLayer layer, Set<ResourceKey<Level>> dimensionFilter, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable Supplier<HolderSet<Biome>> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator, @Nullable List<IndicatorGenerator> indicatorGenerators) {
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
        this.indicatorGenerators = Objects.requireNonNullElseGet(indicatorGenerators, ArrayList::new);

        recomputeBedrockOres();
    }

    @HideFromJS
    public void register(ResourceLocation id) {
        if (GTRegistries.ORE_VEINS.containKey(id)) {
            GTRegistries.ORE_VEINS.replace(id, this);
        } else {
            GTRegistries.ORE_VEINS.register(id, this);
        }
    }

    private void recomputeBedrockOres() {
        this.maximumYield = (int) (density * 100) * clusterSize;
        this.minimumYield = this.maximumYield / 7;
        this.depletedYield = (int) (clusterSize / density / 10);
        this.depletionChance = (int) (weight * density / 5);
    }

    public GTOreDefinition clusterSize(int clusterSize) {
        this.clusterSize = clusterSize;
        recomputeBedrockOres();

        return this;
    }

    public GTOreDefinition density(float density) {
        this.density = density;
        recomputeBedrockOres();

        return this;
    }

    public GTOreDefinition weight(int weight) {
        this.weight = weight;
        recomputeBedrockOres();

        return this;
    }

    public GTOreDefinition layer(IWorldGenLayer layer) {
        this.layer = layer;
        if (this.dimensionFilter == null || this.dimensionFilter.isEmpty()) {
            dimensions(layer.getLevels().toArray(ResourceLocation[]::new));
        }

        return this;
    }

    public GTOreDefinition dimensions(ResourceLocation... dimensions) {
        this.dimensionFilter = Arrays.stream(dimensions)
                .map(location -> ResourceKey.create(Registries.DIMENSION, location))
                .collect(Collectors.toSet());
        return this;
    }

    public GTOreDefinition biomes(String first, String... biomes) {
        // The first param is separate to avoid method confusion with the Lombok-generated fluent getter
        List<String> biomeList = Stream.of(Stream.of(first), Arrays.stream(biomes))
                .flatMap(Function.identity())
                .toList();

        this.biomes = OreVeinUtil.resolveBiomes(biomeList);
        return this;
    }

    public GTOreDefinition biomes(TagKey<Biome> biomes) {
        this.biomes = () -> GTRegistries.builtinRegistry().lookupOrThrow(Registries.BIOME).getOrThrow(biomes);
        return this;
    }

    public GTOreDefinition biomes(Supplier<HolderSet<Biome>> biomes) {
        this.biomes = biomes;
        return this;
    }

    public GTOreDefinition heightRangeUniform(int min, int max) {
        heightRange(HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public GTOreDefinition heightRangeTriangle(int min, int max) {
        heightRange(HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public GTOreDefinition heightRange(HeightRangePlacement range) {
        this.range = range;
        return this;
    }

    public GTOreDefinition standardVeinGenerator(Consumer<StandardVeinGenerator> config) {
        var veinGenerator = new StandardVeinGenerator(this);

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public GTOreDefinition layeredVeinGenerator(Consumer<LayeredVeinGenerator> config) {
        var veinGenerator = new LayeredVeinGenerator(this);

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public GTOreDefinition geodeVeinGenerator(Consumer<GeodeVeinGenerator> config) {
        var veinGenerator = new GeodeVeinGenerator(this);

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public GTOreDefinition dikeVeinGenerator(Consumer<DikeVeinGenerator> config) {
        var veinGenerator = new DikeVeinGenerator(this);
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public GTOreDefinition veinedVeinGenerator(Consumer<VeinedVeinGenerator> config) {
        var veinGenerator = new VeinedVeinGenerator(this);
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    @Nullable
    public VeinGenerator veinGenerator(ResourceLocation id) {
        if (veinGenerator == null) {
            veinGenerator = WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.containsKey(id) ? WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.get(id).apply(this) : null;
        }
        return veinGenerator;
    }

    public GTOreDefinition surfaceIndicatorGenerator(Consumer<SurfaceIndicatorGenerator> config) {
        config.accept(getOrCreateIndicatorGenerator(SurfaceIndicatorGenerator.class, SurfaceIndicatorGenerator::new));
        return this;
    }

    private <T extends IndicatorGenerator> T getOrCreateIndicatorGenerator(Class<T> indicatorClass, Function<GTOreDefinition, T> constructor) {
        var existingGenerator = indicatorGenerators.stream()
                .filter(indicatorClass::isInstance)
                .map(indicatorClass::cast)
                .findFirst().orElse(null);

        if (existingGenerator != null)
            return existingGenerator;

        var generator = constructor.apply(this);
        indicatorGenerators.add(generator);
        return generator;
    }

    @HideFromJS
    public List<Map.Entry<Integer, Material>> getBedrockVeinMaterials() {
        if (bedrockVeinMaterial == null) {
            if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
                bedrockVeinMaterial = this.veinGenerator().getValidMaterialsChances();
            } else {
                bedrockVeinMaterial = List.of();
            }
        }
        return bedrockVeinMaterial;
    }

    private static class InferredProperties {
        public Pair<Integer, Integer> heightRange = null;
    }
}
