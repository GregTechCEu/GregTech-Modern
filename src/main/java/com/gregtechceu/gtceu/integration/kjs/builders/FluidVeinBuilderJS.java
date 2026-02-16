package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class FluidVeinBuilderJS {

    private final ResourceLocation id;
    @Setter
    private int weight; // weight value for determining which vein will appear
    @Setter
    private int minimumYield, maximumYield;// the [minimum, maximum) yields
    @Setter
    private int depletionAmount; // amount of fluid the vein gets drained by
    @Setter
    private int depletionChance = 1; // the chance [0, 100] that the vein will deplete by 1
    @Setter
    private int depletedYield; // yield after the vein is depleted
    @Setter
    private Supplier<Fluid> fluid; // the fluid which the vein contains
    private final List<BiomeWeightModifier> biomes = new LinkedList<>();

    private final transient Set<ResourceKey<Level>> dimensions = new HashSet<>();

    public FluidVeinBuilderJS(ResourceLocation id) {
        this.id = id;
    }

    public FluidVeinBuilderJS yield(int min, int max) {
        return minimumYield(min).maximumYield(max);
    }

    public FluidVeinBuilderJS addSpawnDimension(ResourceLocation... dimensions) {
        for (ResourceLocation dimension : dimensions) {
            this.dimensions.add(ResourceKey.create(Registries.DIMENSION, dimension));
        }
        return this;
    }

    public FluidVeinBuilderJS biomes(int weight, String biomes) {
        Registry<Biome> registry = GTRegistries.builtinRegistry().registry(Registries.BIOME).get();
        this.biomes.add(
                new BiomeWeightModifier(() -> biomes.startsWith("#") ?
                        registry.getOrCreateTag(
                                TagKey.create(Registries.BIOME, new ResourceLocation(biomes.substring(1)))) :
                        (HolderSet.direct(registry
                                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(biomes))))),
                        weight));
        return this;
    }

    public FluidVeinBuilderJS biomes(int weight, String... biomes) {
        Registry<Biome> registry = GTRegistries.builtinRegistry().registry(Registries.BIOME).get();
        List<HolderSet<Biome>> biomeKeys = new LinkedList<>();
        for (String biome : biomes) {
            biomeKeys.add(biome.startsWith("#") ?
                    registry.getOrCreateTag(TagKey.create(Registries.BIOME, new ResourceLocation(biome.substring(1)))) :
                    HolderSet.direct(registry
                            .getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(biome)))));
        }
        this.biomes.add(new BiomeWeightModifier(
                () -> HolderSet.direct(biomeKeys.stream().flatMap(HolderSet::stream).toList()), weight));
        return this;
    }

    @HideFromJS
    public BedrockFluidDefinition build() {
        return new BedrockFluidDefinition(id, weight, minimumYield, maximumYield, depletionAmount, depletionChance,
                depletedYield, fluid, biomes, dimensions);
    }
}
