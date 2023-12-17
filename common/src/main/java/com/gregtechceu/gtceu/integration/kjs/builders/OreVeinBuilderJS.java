package com.gregtechceu.gtceu.integration.kjs.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class OreVeinBuilderJS {
    public final ResourceLocation id;
    @Setter
    public transient int clusterSize, weight;
    @Setter
    public transient float density, discardChanceOnAirExposure;
    @Setter
    public transient IWorldGenLayer layer;
    @Setter
    public transient HeightRangePlacement heightRange;
    @Setter
    public transient BiomeWeightModifier biomeWeightModifier;

    @Setter
    public VeinGenerator generator;
    private final transient List<IndicatorGenerator> indicatorGenerators = new ArrayList<>();

    private final transient Set<ResourceKey<Level>> dimensions = new HashSet<>();
    private final transient List<String> biomeFilter = new ArrayList<>();

    @Getter
    private boolean isBuilt = false;

    public OreVeinBuilderJS(ResourceLocation id) {
        this.id = id;
    }

    public OreVeinBuilderJS dimensions(ResourceLocation... dimensions) {
        for (ResourceLocation dimension : dimensions) {
            this.dimensions.add(ResourceKey.create(Registries.DIMENSION, dimension));
        }
        return this;
    }

    public OreVeinBuilderJS biomes(String... biomes) {
        this.biomeFilter.addAll(Arrays.asList(biomes));
        return this;
    }

    public OreVeinBuilderJS heightRangeUniform(int min, int max) {
        heightRange(HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        return this;
    }

    public OreVeinBuilderJS heightRangeTriangle(int min, int max) {
        heightRange(HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
        return this;
    }

    public OreVeinBuilderJS addIndicator(IndicatorGenerator indicatorGenerator) {
        indicatorGenerators.add(indicatorGenerator);
        return this;
    }

    public VeinGenerator generatorBuilder(ResourceLocation id) {
        return build().generator(id);
    }

    @HideFromJS
    public static OreVeinBuilderJS fromDefinition(ResourceLocation id, GTOreDefinition definition) {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        OreVeinBuilderJS builder = new OreVeinBuilderJS(id);

        builder.clusterSize = definition.getClusterSize();
        builder.density = definition.getDensity();
        builder.weight = definition.getWeight();
        builder.layer = definition.getLayer();
        builder.dimensions.addAll(definition.getDimensionFilter());
        builder.heightRange = definition.getRange();
        builder.discardChanceOnAirExposure = definition.getDiscardChanceOnAirExposure();
        builder.biomeWeightModifier = definition.getBiomeWeightModifier();
        builder.generator = definition.getVeinGenerator();
        builder.indicatorGenerators.addAll(definition.getIndicatorGenerators());


        Supplier<HolderSet<Biome>> biomes = definition.getBiomes();
        if (biomes != null) {
            JsonElement element = RegistryCodecs.homogeneousList(Registries.BIOME)
                    .encode(biomes.get(), registryOps, registryOps.empty())
                    .getOrThrow(false, e -> {});

            if (element.isJsonArray()) {
                builder.biomeFilter.addAll(element.getAsJsonArray().asList().stream()
                        .map(JsonElement::getAsString)
                        .toList()
                );
            } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                builder.biomeFilter.add(element.getAsString());
            } else {
                GTCEu.LOGGER.error("Cannot add biome filter from json element");
            }
        }

        return builder;
    }

    @HideFromJS
    public GTOreDefinition build() {
        isBuilt = true;
        return new GTOreDefinition(
                id, clusterSize, density, weight, layer, resolveDimensions(),
                heightRange, discardChanceOnAirExposure, resolveBiomes(),
                biomeWeightModifier, generator,  indicatorGenerators
        );
    }

    private Set<ResourceKey<Level>> resolveDimensions() {
        if (!this.dimensions.isEmpty())
            return this.dimensions;

        return layer.getLevels().stream()
                .map(location -> ResourceKey.create(Registries.DIMENSION, location))
                .collect(Collectors.toSet());
    }

    @Nullable
    private Supplier<HolderSet<Biome>> resolveBiomes() {
        if (biomeFilter.isEmpty())
            return null;

        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return () -> RegistryCodecs.homogeneousList(Registries.BIOME)
                .decode(registryOps, resolveBiomeCodecInput())
                .map(Pair::getFirst)
                .getOrThrow(false, GTCEu.LOGGER::error);
    }

    private JsonElement resolveBiomeCodecInput() {
        if (biomeFilter.size() == 1)
            return new JsonPrimitive(biomeFilter.get(0));

        if (biomeFilter.stream().anyMatch(filter -> filter.startsWith("#")))
            throw new IllegalStateException("Cannot resolve biomes for vein " + id + ": You may use either a single tag or multiple individual biomes.");

        var jsonArray = new JsonArray();
        biomeFilter.forEach(jsonArray::add);
        return jsonArray;
    }

}
