package com.gregtechceu.gtceu.integration.kjs.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
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
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class OreVeinBuilderJS {
    private final ResourceLocation id;
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
    private final transient Set<ResourceKey<Level>> dimensions = new HashSet<>();

    private final transient JsonArray biomeFilter = new JsonArray();
    @Getter
    private boolean isBuilt = false;

    public OreVeinBuilderJS(ResourceLocation id) {
        this.id = id;
    }

    public OreVeinBuilderJS addSpawnDimension(ResourceLocation dimension) {
        dimensions.add(ResourceKey.create(Registries.DIMENSION, dimension));
        return this;
    }

    public OreVeinBuilderJS addSpawnBiome(String biome) {
        biomeFilter.add(biome);
        return this;
    }

    public VeinGenerator generatorBuilder(ResourceLocation id) {
        return build().generator(id);
    }

    @HideFromJS
    public GTOreDefinition build() {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        Supplier<HolderSet<Biome>> biomes = () -> RegistryCodecs.homogeneousList(Registries.BIOME)
                .decode(registryOps, biomeFilter.size() == 1 ? biomeFilter.get(0) : biomeFilter).map(Pair::getFirst).getOrThrow(false, GTCEu.LOGGER::error);
        isBuilt = true;
        return new GTOreDefinition(id, clusterSize, density, weight, layer, dimensions, heightRange, discardChanceOnAirExposure, biomes, biomeWeightModifier, generator);
    }

}
