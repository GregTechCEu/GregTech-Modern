package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.RegistryUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Accessors(fluent = true, chain = true)
public class BedrockOreDefinition {

    public static final Codec<BedrockOreDefinition> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    Codec.INT.fieldOf("size").forGetter(ft -> ft.size),
                    IntProvider.POSITIVE_CODEC.fieldOf("yield").forGetter(ft -> ft.yield),
                    Codec.INT.fieldOf("depletion_amount").forGetter(ft -> ft.depletionAmount),
                    ExtraCodecs.intRange(0, 100).fieldOf("depletion_chance").forGetter(ft -> ft.depletionChance),
                    Codec.INT.fieldOf("depleted_yield").forGetter(ft -> ft.depletedYield),
                    WeightedMaterial.CODEC.listOf().fieldOf("materials").forGetter(ft -> ft.materials),
                    BiomeWeightModifier.CODEC.listOf().optionalFieldOf("weight_modifier", List.of())
                            .forGetter(ft -> ft.originalModifiers),
                    ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter")
                            .forGetter(ft -> new ArrayList<>(ft.dimensionFilter)))
                    .apply(instance,
                            (weight, size, yield, depletionAmount, depletionChance, depletedYield, materials,
                             biomeWeightModifier, dimensionFilter) -> new BedrockOreDefinition(weight, size, yield,
                                     depletionAmount, depletionChance, depletedYield, materials, biomeWeightModifier,
                                     new HashSet<>(dimensionFilter))));

    @Getter
    @Setter
    private int weight; // weight value for determining which vein will appear
    @Getter
    @Setter
    private int size; // size in chunks
    @Getter
    @Setter
    private IntProvider yield;// the [minimum, maximum] yields
    @Getter
    @Setter
    private int depletionAmount; // amount of ore the vein gets drained by
    @Getter
    @Setter
    private int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    @Getter
    @Setter
    private int depletedYield; // yield after the vein is depleted
    @Getter
    @Setter
    private List<WeightedMaterial> materials; // the ores which the vein contains
    @Getter
    private BiomeWeightModifier biomeWeightModifier; // weighting of biomes
    private List<BiomeWeightModifier> originalModifiers; // weighting of biomes
    @Getter
    @Setter
    public Set<ResourceKey<Level>> dimensionFilter; // filtering of dimensions

    public BedrockOreDefinition(ResourceLocation name, int size, int weight, IntProvider yield, int depletionAmount,
                                int depletionChance, int depletedYield, List<WeightedMaterial> materials,
                                List<BiomeWeightModifier> originalModifiers, Set<ResourceKey<Level>> dimensionFilter) {
        this(weight, size, yield, depletionAmount, depletionChance, depletedYield, materials, originalModifiers,
                dimensionFilter);
        GTRegistries.BEDROCK_ORE_DEFINITIONS.register(name, this);
    }

    public BedrockOreDefinition(int weight, int size, IntProvider yield, int depletionAmount, int depletionChance,
                                int depletedYield, List<WeightedMaterial> materials,
                                List<BiomeWeightModifier> originalModifiers, Set<ResourceKey<Level>> dimensionFilter) {
        this.weight = weight;
        this.size = size;
        this.yield = yield;
        this.depletionAmount = depletionAmount;
        this.depletionChance = depletionChance;
        this.depletedYield = depletedYield;
        this.materials = materials;
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

    public IntList getAllChances() {
        return IntArrayList.toList(materials().stream().mapToInt(WeightedMaterial::weight));
    }

    public List<Material> getAllMaterials() {
        return materials().stream().map(WeightedMaterial::material).toList();
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
        private List<WeightedMaterial> materials; // the ores which the vein contains
        private Set<ResourceKey<Level>> dimensions;
        private final List<BiomeWeightModifier> biomes = new LinkedList<>();

        private Builder(ResourceLocation name) {
            this.name = name;
        }

        public Builder copy(ResourceLocation name) {
            var copied = new Builder(name);
            copied.weight = weight;
            copied.yield = yield;
            copied.depletionAmount = depletionAmount;
            copied.depletionChance = depletionChance;
            copied.depletedYield = depletedYield;
            copied.materials = materials;
            return copied;
        }

        public Builder material(Material material, int amount) {
            if (this.materials == null) this.materials = new ArrayList<>();
            this.materials.add(new WeightedMaterial(material, amount));
            return this;
        }

        public Builder yield(int min, int max) {
            return this.yield(UniformInt.of(min, max));
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

        public BedrockOreDefinition register() {
            var definition = new BedrockOreDefinition(weight, size, yield, depletionAmount, depletionChance,
                    depletedYield, materials, biomes, dimensions);
            GTRegistries.BEDROCK_ORE_DEFINITIONS.registerOrOverride(name, definition);
            return definition;
        }
    }
}
