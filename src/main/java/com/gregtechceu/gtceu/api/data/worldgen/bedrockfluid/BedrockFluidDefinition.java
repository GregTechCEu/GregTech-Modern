package com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.RegistryUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Supplier;

public class BedrockFluidDefinition {

    public static final MapCodec<Pair<Integer, Integer>> YIELD = Codec.mapPair(Codec.INT.fieldOf("min"),
            Codec.INT.fieldOf("max"));

    public static final Codec<BedrockFluidDefinition> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    YIELD.fieldOf("yield").forGetter(ft -> Pair.of(ft.minimumYield, ft.maximumYield)),
                    Codec.INT.fieldOf("depletion_amount").forGetter(ft -> ft.depletionAmount),
                    Codec.INT.fieldOf("depletion_chance").forGetter(ft -> ft.depletionChance),
                    Codec.INT.fieldOf("depleted_yield").forGetter(ft -> ft.depletedYield),
                    BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(ft -> ft.storedFluid.get()),
                    BiomeWeightModifier.CODEC.listOf().optionalFieldOf("weight_modifier", List.of())
                            .forGetter(ft -> ft.originalModifiers),
                    ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter")
                            .forGetter(ft -> new ArrayList<>(ft.dimensionFilter)))
                    .apply(instance,
                            (weight, yield, depletionAmount, depletionChance, depletedYield, storedFluid,
                             biomeWeightModifier, dimensionFilter) -> new BedrockFluidDefinition(weight,
                                     yield.getFirst(), yield.getSecond(), depletionAmount, depletionChance,
                                     depletedYield, () -> storedFluid, biomeWeightModifier,
                                     new HashSet<>(dimensionFilter))));

    @Getter
    @Setter
    private int weight; // weight value for determining which vein will appear
    @Getter
    @Setter
    private int minimumYield, maximumYield;// the [minimum, maximum) yields
    @Getter
    @Setter
    private int depletionAmount; // amount of fluid the vein gets drained by
    @Getter
    @Setter
    private int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    @Getter
    @Setter
    private int depletedYield; // yield after the vein is depleted
    @Getter
    @Setter
    private Supplier<Fluid> storedFluid; // the fluid which the vein contains
    @Getter
    private BiomeWeightModifier biomeWeightModifier; // weighting of biomes
    private List<BiomeWeightModifier> originalModifiers; // weighting of biomes
    @Getter
    @Setter
    public Set<ResourceKey<Level>> dimensionFilter; // filtering of dimensions

    public BedrockFluidDefinition(ResourceLocation name, int weight, int minimumYield, int maximumYield,
                                  int depletionAmount, int depletionChance, int depletedYield,
                                  Supplier<Fluid> storedFluid, List<BiomeWeightModifier> originalModifiers,
                                  Set<ResourceKey<Level>> dimensionFilter) {
        this(weight, minimumYield, maximumYield, depletionAmount, depletionChance, depletedYield, storedFluid,
                originalModifiers, dimensionFilter);
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.register(name, this);
    }

    public BedrockFluidDefinition(int weight, int minimumYield, int maximumYield, int depletionAmount,
                                  int depletionChance, int depletedYield, Supplier<Fluid> storedFluid,
                                  List<BiomeWeightModifier> originalModifiers,
                                  Set<ResourceKey<Level>> dimensionFilter) {
        this.weight = weight;
        this.minimumYield = minimumYield;
        this.maximumYield = maximumYield;
        this.depletionAmount = depletionAmount;
        this.depletionChance = depletionChance;
        this.depletedYield = depletedYield;
        this.storedFluid = storedFluid;
        this.originalModifiers = originalModifiers;
        this.biomeWeightModifier = new BiomeWeightModifier(
                () -> HolderSet.direct(originalModifiers.stream().flatMap(mod -> mod.biomes.get().stream()).toList()),
                originalModifiers.stream().mapToInt(mod -> mod.addedWeight).sum()) {

            @Override
            public int applyAsInt(Holder<Biome> biome) {
                int mod = 0;
                for (var modifier : originalModifiers) {
                    if (modifier.biomes.get().contains(biome)) {
                        mod += modifier.applyAsInt(biome);
                    }
                }
                return mod;
            }
        };
        this.dimensionFilter = dimensionFilter;
    }

    public void setOriginalModifiers(List<BiomeWeightModifier> modifiers) {
        this.originalModifiers = modifiers;
        this.biomeWeightModifier = new BiomeWeightModifier(
                () -> HolderSet.direct(originalModifiers.stream().flatMap(mod -> mod.biomes.get().stream()).toList()),
                originalModifiers.stream().mapToInt(mod -> mod.addedWeight).sum()) {

            @Override
            public int applyAsInt(Holder<Biome> biome) {
                int mod = 0;
                for (var modifier : originalModifiers) {
                    if (modifier.biomes.get().contains(biome)) {
                        mod += modifier.applyAsInt(biome);
                    }
                }
                return mod;
            }
        };
    }

    public static Builder builder(ResourceLocation name) {
        return new Builder(name);
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
        private Set<ResourceKey<Level>> dimensions;
        private final List<BiomeWeightModifier> biomes = new LinkedList<>();

        private Builder(ResourceLocation name) {
            this.name = name;
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

        public Builder biomes(int weight, TagKey<Biome> biomes) {
            this.biomes.add(new BiomeWeightModifier(() -> GTRegistries.builtinRegistry()
                    .registryOrThrow(Registries.BIOME).getOrCreateTag(biomes), weight));
            return this;
        }

        @SafeVarargs
        public final Builder biomes(int weight, ResourceKey<Biome>... biomes) {
            this.biomes.add(new BiomeWeightModifier(() -> HolderSet.direct(GTRegistries.builtinRegistry()
                    .registryOrThrow(Registries.BIOME)::getHolderOrThrow, biomes), weight));
            return this;
        }

        public Builder biomes(int weight, HolderSet<Biome> biomes) {
            this.biomes.add(new BiomeWeightModifier(() -> biomes, weight));
            return this;
        }

        @HideFromJS
        public Builder dimensions(Set<ResourceKey<Level>> dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public Builder dimensions(String... dimensions) {
            return this.dimensions(new HashSet<>(RegistryUtil.resolveResourceKeys(Registries.DIMENSION, dimensions)));
        }

        @ApiStatus.Internal
        public BedrockFluidDefinition build() {
            return new BedrockFluidDefinition(weight, minimumYield, maximumYield, depletionAmount,
                    depletionChance, depletedYield, fluid, biomes, dimensions);
        }

        public BedrockFluidDefinition register() {
            var definition = build();
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.registerOrOverride(name, definition);
            return definition;
        }
    }
}
