package com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BedrockFluidDefinition {
    @Getter
    private final ResourceLocation name;
    @Getter
    private final int weight; // weight value for determining which vein will appear
    @Getter
    private final int minimumYield, maximumYield;// the [minimum, maximum) yields
    @Getter
    private final int depletionAmount; // amount of fluid the vein gets drained by
    @Getter
    private final int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    @Getter
    private final int depletedYield; // yield after the vein is depleted
    @Getter
    private final Supplier<Fluid> storedFluid; // the fluid which the vein contains
    @Getter
    private final Function<Holder<Biome>, Integer> biomeWeightModifier; // weighting of biomes
    @Getter
    private final Predicate<ResourceKey<Level>> dimensionFilter; // filtering of dimensions

    public BedrockFluidDefinition(ResourceLocation name, int weight, int minimumYield, int maximumYield, int depletionAmount, int depletionChance, int depletedYield, Supplier<Fluid> storedFluid, Function<Holder<Biome>, Integer> biomeWeightModifier, Predicate<ResourceKey<Level>> dimensionFilter) {
        this.name = name;
        this.weight = weight;
        this.minimumYield = minimumYield;
        this.maximumYield = maximumYield;
        this.depletionAmount = depletionAmount;
        this.depletionChance = depletionChance;
        this.depletedYield = depletedYield;
        this.storedFluid = storedFluid;
        this.biomeWeightModifier = biomeWeightModifier;
        this.dimensionFilter = dimensionFilter;
    }

    @Accessors(chain = true, fluent = true)
    public static class Builder {
        private final ResourceLocation name;
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
        private final Set<ResourceLocation> dimensions = new HashSet<>();
        private final Map<ResourceLocation, Integer> biomes = new HashMap<>();
        private final Map<TagKey<Biome>, Integer> biomeTags = new HashMap<>();

        private Builder(ResourceLocation name) {
            this.name = name;
        }

        public static Builder create(ResourceLocation name) {
            return new Builder(name);
        }

        public Builder copy(ResourceLocation name) {
            var copied = new Builder(name);
            copied.weight = weight;
            copied.minimumYield = minimumYield;
            copied.maximumYield = maximumYield;
            copied.depletionAmount = depletionAmount;
            copied.depletionChance = depletionChance;
            copied.depletedYield = depletedYield;
            copied.fluid = fluid;
            return copied;
        }

        public Builder yield(int min, int max) {
            return minimumYield(min).maximumYield(max);
        }

        @SafeVarargs
        public final Builder dimensions(ResourceKey<Level>... levels) {
            for (ResourceKey<Level> level : levels) {
                dimensions.add(level.location());
            }
            return this;
        }

        public final Builder dimensions(ResourceLocation... levels) {
            dimensions.addAll(Arrays.asList(levels));
            return this;
        }

        @SafeVarargs
        public final Builder biomes(int weight, TagKey<Biome>... biomes) {
            for (var biome : biomes) {
                this.biomeTags.put(biome, weight);
            }
            return this;
        }

        @SafeVarargs
        public final Builder biomes(int weight, ResourceKey<Biome>... biomes) {
            for (var biome : biomes) {
                this.biomes.put(biome.location(), weight);
            }
            return this;
        }

        public final Builder biomes(int weight, ResourceLocation... biomes) {
            for (var biome : biomes) {
                this.biomes.put(biome, weight);
            }
            return this;
        }

        public BedrockFluidDefinition register() {
            var definition = new BedrockFluidDefinition(name, weight, minimumYield, maximumYield, depletionAmount, depletionChance, depletedYield, fluid,
                    biomeHolder -> {
                        for (var biome : biomes.entrySet()) {
                            if (biomeHolder.is(biome.getKey())) {
                                return biome.getValue();
                            }
                        }
                        for (var biome : biomeTags.entrySet()) {
                            if (biomeHolder.is(biome.getKey())) {
                                return biome.getValue();
                            }
                        }
                        return 0;
                    },
                    dimension -> dimensions.isEmpty() || dimensions.contains(dimension.location()));
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.register(definition.name, definition);
            return definition;
        }
    }

}
