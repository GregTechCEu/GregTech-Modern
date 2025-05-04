package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Accessors(chain = true, fluent = true)
public class BedrockFluidBuilder extends BuilderBase<BedrockFluidDefinition> {

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
    private Fluid fluid; // the fluid which the vein contains
    private final List<BiomeWeightModifier> biomes = new LinkedList<>();

    private final transient Set<ResourceKey<Level>> dimensions = new HashSet<>();

    public BedrockFluidBuilder(ResourceLocation id) {
        super(id);
    }

    public static BedrockFluidBuilder from(BedrockFluidDefinition definition, ResourceLocation id) {
        var builder = new BedrockFluidBuilder(id);
        builder.weight(definition.getWeight());
        builder.minimumYield(definition.getMinimumYield()).maximumYield(definition.getMaximumYield());
        builder.depletionAmount(definition.getDepletionAmount()).depletionChance(definition.getDepletionChance());
        builder.depletedYield(definition.getDepletedYield());
        builder.fluid(definition.getStoredFluid());
        builder.dimensions.addAll(definition.getDimensionFilter());
        builder.biomes.addAll(definition.getOriginalModifiers());
        return builder;
    }

    public BedrockFluidBuilder yield(int min, int max) {
        return minimumYield(min).maximumYield(max);
    }

    @SafeVarargs
    public final BedrockFluidBuilder addSpawnDimension(ResourceKey<Level>... dimensions) {
        this.dimensions.addAll(Arrays.asList(dimensions));
        return this;
    }

    public BedrockFluidBuilder biomes(int weight, HolderSet<Biome> biomes) {
        this.biomes.add(new BiomeWeightModifier(biomes, weight));
        return this;
    }

    @Override
    public BedrockFluidDefinition createObject() {
        return new BedrockFluidDefinition(weight, minimumYield, maximumYield, depletionAmount, depletionChance,
                depletedYield, fluid, BiomeWeightModifier.fromList(biomes), dimensions);
    }
}
