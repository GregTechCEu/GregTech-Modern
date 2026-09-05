package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.HolderGetter;
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
import dev.latvian.mods.rhino.util.RemapForJS;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
@SuppressWarnings("unused")
public class OreVeinDefinitionBuilderJS extends BuilderBase<GTOreDefinition> {

    private final InferredProperties inferredProperties = new InferredProperties();

    @Setter
    private IntProvider clusterSize;
    @Setter
    private float density;
    @Setter
    private int weight;
    private IWorldGenLayer layer = WorldGenLayers.STONE;
    @Setter
    private Set<ResourceKey<Level>> dimensionFilter = Set.of();
    @Setter
    private HeightRangePlacement heightRange;
    @Setter
    private float discardChanceOnAirExposure;
    @Setter
    @Nullable
    private HolderSet<Biome> biomes;
    @Setter
    private @Nullable BiomeWeightModifier biomeWeightModifier;

    @Setter
    @Nullable
    private VeinGenerator veinGenerator;
    @Setter
    private List<IndicatorGenerator> indicatorGenerators = new ArrayList<>();

    public OreVeinDefinitionBuilderJS(ResourceLocation id) {
        super(id);
    }

    @Tolerate
    public OreVeinDefinitionBuilderJS clusterSize(int clusterSize) {
        this.clusterSize = ConstantInt.of(clusterSize);
        return this;
    }

    public OreVeinDefinitionBuilderJS layer(IWorldGenLayer layer) {
        this.layer = layer;
        if (this.dimensionFilter.isEmpty()) {
            dimensions(layer.getLevels());
        }
        return this;
    }

    public OreVeinDefinitionBuilderJS dimensions(Collection<?> dimensions) {
        Set<ResourceKey<Level>> keys = new HashSet<>();
        for (Object dim : dimensions) {
            keys.add(wrapDimension(dim));
        }
        this.dimensionFilter = keys;
        return this;
    }

    @SuppressWarnings("unchecked")
    private static ResourceKey<Level> wrapDimension(Object dim) {
        if (dim instanceof ResourceKey<?> key) return (ResourceKey<Level>) key;
        if (dim instanceof ResourceLocation rl) return ResourceKey.create(Registries.DIMENSION, rl);
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim.toString()));
    }

    public OreVeinDefinitionBuilderJS heightRangeUniform(int min, int max) {
        heightRange(HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public OreVeinDefinitionBuilderJS heightRangeTriangle(int min, int max) {
        heightRange(HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        inferredProperties.heightRange = Pair.of(min, max);
        return this;
    }

    public OreVeinDefinitionBuilderJS standardVeinGenerator(Consumer<StandardVeinGenerator> config) {
        var veinGenerator = new StandardVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilderJS layeredVeinGenerator(Consumer<LayeredVeinGenerator> config) {
        var veinGenerator = new LayeredVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilderJS geodeVeinGenerator(Consumer<GeodeVeinGenerator> config) {
        var veinGenerator = new GeodeVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilderJS dikeVeinGenerator(Consumer<DikeVeinGenerator> config) {
        var veinGenerator = new DikeVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilderJS veinedVeinGenerator(Consumer<VeinedVeinGenerator> config) {
        var veinGenerator = new VeinedVeinGenerator();
        if (inferredProperties.heightRange != null) {
            veinGenerator.minYLevel(inferredProperties.heightRange.getFirst());
            veinGenerator.maxYLevel(inferredProperties.heightRange.getSecond());
        }

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilderJS classicVeinGenerator(Consumer<ClassicVeinGenerator> config) {
        var veinGenerator = new ClassicVeinGenerator();

        config.accept(veinGenerator);
        this.veinGenerator = veinGenerator;

        return this;
    }

    public OreVeinDefinitionBuilderJS cuboidVeinGenerator(Consumer<CuboidVeinGenerator> config) {
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
    @RemapForJS("customVeinGenerator")
    public VeinGenerator veinGenerator(ResourceLocation id) {
        if (veinGenerator == null) {
            VeinGenerator.VeinGeneratorType<?> type = GTRegistries.VEIN_GENERATORS.get(id);
            veinGenerator = type == null ? null : type.defaultInstance().get();
        }
        return veinGenerator;
    }

    public OreVeinDefinitionBuilderJS surfaceIndicatorGenerator(Consumer<SurfaceIndicatorGenerator> config) {
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
    public GTOreDefinition createObject() {
        var registries = RegistryAccessContainer.current;
        HolderGetter<Biome> biomeParse = registries == null ? null :
                registries.access().lookupOrThrow(Registries.BIOME);
        return new GTOreDefinition(clusterSize, density, weight, layer,
                Set.copyOf(dimensionFilter), heightRange, discardChanceOnAirExposure,
                biomes == null ? HolderSet.empty() : biomes,
                biomeWeightModifier == null ? BiomeWeightModifier.EMPTY : biomeWeightModifier,
                veinGenerator, indicatorGenerators,
                biomeParse);
    }
}
