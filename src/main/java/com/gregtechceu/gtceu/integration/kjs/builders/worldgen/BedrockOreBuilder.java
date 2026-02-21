package com.gregtechceu.gtceu.integration.kjs.builders.worldgen;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.WeightedMaterial;

import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Accessors(chain = true, fluent = true)
public class BedrockOreBuilder extends BuilderBase<BedrockOreDefinition> {

    @Setter
    private int weight; // weight value for determining which vein will appear
    @Setter
    private int size; // size of the vein, in chunks.
    @Setter
    private IntProvider yield;// the [minimum, maximum) yields
    @Setter
    private int depletionAmount; // amount of fluid the vein gets drained by
    @Setter
    private int depletionChance = 1; // the chance [0, 100] that the vein will deplete by 1
    @Setter
    private int depletedYield; // yield after the vein is depleted
    @Setter
    private List<WeightedMaterial> materials = new ArrayList<>(); // the ores which the vein contains
    private final transient Set<ResourceKey<Level>> dimensions = new HashSet<>();
    private final List<BiomeWeightModifier> biomes = new LinkedList<>();

    public BedrockOreBuilder(ResourceLocation id) {
        super(id);
    }

    public static BedrockOreBuilder from(BedrockOreDefinition definition, ResourceLocation id) {
        var builder = new BedrockOreBuilder(id);
        builder.weight(definition.weight());
        builder.yield(definition.yield());
        builder.depletionAmount(definition.depletionAmount());
        builder.depletionChance(definition.depletionChance());
        builder.depletedYield(definition.depletedYield());
        builder.materials(definition.materials());
        builder.dimensions.addAll(definition.dimensionFilter());
        builder.biomes.addAll(definition.getOriginalModifiers());
        return builder;
    }

    public BedrockOreBuilder material(Material material, int amount) {
        this.materials.add(new WeightedMaterial(material, amount));
        return this;
    }

    public BedrockOreBuilder yield(int min, int max) {
        return this.yield(UniformInt.of(min, max));
    }

    @SafeVarargs
    public final BedrockOreBuilder addSpawnDimension(ResourceKey<Level>... dimensions) {
        this.dimensions.addAll(Arrays.asList(dimensions));
        return this;
    }

    public BedrockOreBuilder biomes(int weight, HolderSet<Biome> biomes) {
        this.biomes.add(new BiomeWeightModifier(biomes, weight));
        return this;
    }

    @Override
    public BedrockOreDefinition createObject() {
        return new BedrockOreDefinition(weight, size, yield, depletionAmount, depletionChance,
                depletedYield, materials, BiomeWeightModifier.fromList(biomes), dimensions);
    }
}
