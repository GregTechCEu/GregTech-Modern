package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.worldgen.*;
import com.gregtechceu.gtceu.api.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.veins.*;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class OreVeinDefinitionBuilder extends BuilderBase<OreVeinDefinition> {

    private final InferredProperties inferredProperties = new InferredProperties();

    @Setter
    private IntProvider clusterSize;
    @Setter
    private float density;
    @Setter
    private int weight;
    private IWorldGenLayer layer = WorldGenLayers.STONE;
    @Setter
    private Set<ResourceKey<Level>> dimensionFilter;
    @Setter
    private HeightRangePlacement heightRange;
    @Setter
    private float discardChanceOnAirExposure;
    @Setter
    @Nullable
    private HolderSet<Biome> biomes;
    @Setter
    private BiomeWeightModifier biomeWeightModifier;

    @Setter
    @Nullable
    private VeinGenerator veinGenerator;
    @Setter
    private List<IndicatorGenerator> indicatorGenerators;

    public OreVeinDefinitionBuilder(ResourceLocation id) {
        super(id);
    }

    @Tolerate
    public OreVeinDefinitionBuilder clusterSize(int clusterSize) {
        this.clusterSize = ConstantInt.of(clusterSize);
        return this;
    }

    public OreVeinDefinitionBuilder layer(IWorldGenLayer layer) {
        this.layer = layer;
        if (this.dimensionFilter.isEmpty()) {
            dimensions(layer.getLevels());
        }
        return this;
    }

    public OreVeinDefinitionBuilder dimensions(Collection<ResourceKey<Level>> dimensions) {
        this.dimensionFilter = new HashSet<>(dimensions);
        return this;
    }

    public OreVeinDefinitionBuilder heightRangeUniform(int min, int max) {
        heightRange(HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public OreVeinDefinitionBuilder heightRangeTriangle(int min, int max) {
        heightRange(HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public OreVeinDefinitionBuilder standardVeinGenerator(Consumer<StandardVeinGenerator> config) {
        var veinGenerator = new StandardVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilder layeredVeinGenerator(Consumer<LayeredVeinGenerator> config) {
        var veinGenerator = new LayeredVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilder geodeVeinGenerator(Consumer<GeodeVeinGenerator> config) {
        var veinGenerator = new GeodeVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilder dikeVeinGenerator(Consumer<DikeVeinGenerator> config) {
        var veinGenerator = new DikeVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilder veinedVeinGenerator(Consumer<VeinedVeinGenerator> config) {
        var veinGenerator = new VeinedVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilder classicVeinGenerator(Consumer<ClassicVeinGenerator> config) {
        var veinGenerator = new ClassicVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilder cuboidVeinGenerator(Consumer<CuboidVeinGenerator> config) {
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

    public OreVeinDefinitionBuilder surfaceIndicatorGenerator(Consumer<SurfaceIndicatorGenerator> config) {
        config.accept(getOrCreateIndicatorGenerator(SurfaceIndicatorGenerator.class, SurfaceIndicatorGenerator::new));
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends IndicatorGenerator> T getOrCreateIndicatorGenerator(Class<T> indicatorClass,
                                                                           Supplier<T> constructor) {
        @Nullable
        var existingGenerator = indicatorGenerators.stream()
                .filter(indicatorClass::isInstance)
                .map(indicatorClass::cast)
                .findFirst()
                .orElse(null);

        if (existingGenerator != null)
            return existingGenerator;

        var generator = constructor.get();
        indicatorGenerators.add(generator);
        return generator;
    }

    private static class InferredProperties {

        @Nullable
        public Pair<Integer, Integer> heightRange = null;
    }

    // It's simpler than doing the exact same thing via a ton of nested calls.
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public OreVeinDefinition createObject() {
        return new OreVeinDefinition(clusterSize, density, weight, layer,
                Set.copyOf(dimensionFilter), heightRange, discardChanceOnAirExposure,
                biomes, biomeWeightModifier, veinGenerator, indicatorGenerators,
                RegistryAccessContainer.current.access().lookupOrThrow(Registries.BIOME));
    }
}
