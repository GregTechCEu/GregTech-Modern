package com.gregtechceu.gtceu.api.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.BiomeWeightModifier;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.util.*;

@Accessors(fluent = true, chain = true)
public class BedrockOreDefinition {

    // spotless:off
    public static final MapCodec<Pair<Material, Integer>> MATERIAL = Codec
            .mapPair(GTRegistries.MATERIALS.byNameCodec().fieldOf("material"), Codec.INT.fieldOf("chance"));

    public static final Codec<BedrockOreDefinition> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("weight").forGetter(BedrockOreDefinition::weight),
            Codec.INT.fieldOf("size").forGetter(BedrockOreDefinition::size),
            IntProvider.POSITIVE_CODEC.fieldOf("yield").forGetter(BedrockOreDefinition::yield),
            Codec.INT.fieldOf("depletion_amount").forGetter(BedrockOreDefinition::depletionAmount),
            ExtraCodecs.intRange(0, 100).fieldOf("depletion_chance").forGetter(BedrockOreDefinition::depletionChance),
            Codec.INT.fieldOf("depleted_yield").forGetter(BedrockOreDefinition::depletedYield),
            MATERIAL.codec().listOf().fieldOf("materials").forGetter(BedrockOreDefinition::materials),
            BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", BiomeWeightModifier.EMPTY).forGetter(BedrockOreDefinition::biomeWeightModifier),
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter").forGetter(ft -> new ArrayList<>(ft.dimensionFilter))
            ).apply(instance, BedrockOreDefinition::new));
    public static final Codec<Holder<BedrockOreDefinition>> CODEC = RegistryFixedCodec.create(GTRegistries.BEDROCK_ORE_REGISTRY);
    // spotless:on
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
    private List<Pair<Material, Integer>> materials; // the ores which the vein contains
    @Getter
    @Setter
    private BiomeWeightModifier biomeWeightModifier; // weighting of biomes
    @Getter
    @Setter
    public Set<ResourceKey<Level>> dimensionFilter; // filtering of dimensions

    public BedrockOreDefinition(int weight, int size, IntProvider yield, int depletionAmount, int depletionChance,
                                int depletedYield, List<Pair<Material, Integer>> materials,
                                BiomeWeightModifier biomeWeightModifier, List<ResourceKey<Level>> dimensionFilter) {
        this(weight, size, yield, depletionAmount, depletionChance, depletedYield, materials, biomeWeightModifier,
                new HashSet<>(dimensionFilter));
    }

    public BedrockOreDefinition(int weight, int size, IntProvider yield, int depletionAmount, int depletionChance,
                                int depletedYield, List<Pair<Material, Integer>> materials,
                                BiomeWeightModifier biomeWeightModifier, Set<ResourceKey<Level>> dimensionFilter) {
        this.weight = weight;
        this.size = size;
        this.yield = yield;
        this.depletionAmount = depletionAmount;
        this.depletionChance = depletionChance;
        this.depletedYield = depletedYield;
        this.materials = materials;
        this.biomeWeightModifier = biomeWeightModifier;
        this.dimensionFilter = dimensionFilter;
    }

    @Tolerate
    public void biomeWeightModifier(List<BiomeWeightModifier> modifiers) {
        this.biomeWeightModifier = BiomeWeightModifier.fromList(modifiers);
    }

    public List<Integer> getAllChances() {
        return materials().stream().map(Pair::getSecond).toList();
    }

    public List<Material> getAllMaterials() {
        return materials().stream().map(Pair::getFirst).toList();
    }

    public boolean canGenerate() {
        return this.weight() > 0 || !this.biomeWeightModifier().isEmpty();
    }

    public List<BiomeWeightModifier> getOriginalModifiers() {
        if (this.biomeWeightModifier instanceof BiomeWeightModifier.FromList list) {
            return list.getOriginalModifiers();
        } else {
            return Collections.singletonList(this.biomeWeightModifier);
        }
    }

    public static Builder builder(HolderGetter<Biome> biomeLookup) {
        return new Builder(biomeLookup);
    }

    public Builder asBuilder(HolderGetter<Biome> biomeLookup) {
        Builder builder = builder(biomeLookup);
        builder.weight(this.weight);
        builder.size(this.size);
        builder.yield(this.yield);
        builder.depletionAmount(this.depletionAmount).depletionChance(this.depletionChance);
        builder.depletedYield(this.depletedYield);
        builder.materials(this.materials);
        builder.dimensions(this.dimensionFilter);
        builder.biomes(this.getOriginalModifiers());

        return builder;
    }

    @Accessors(chain = true, fluent = true)
    public static class Builder {

        private final HolderGetter<Biome> biomeLookup;

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
        private List<Pair<Material, Integer>> materials = new ArrayList<>(); // the ores which the vein contains
        @Setter
        private Set<ResourceKey<Level>> dimensions = Collections.emptySet();
        private final List<BiomeWeightModifier> biomes = new LinkedList<>();

        private Builder(HolderGetter<Biome> biomeLookup) {
            this.biomeLookup = biomeLookup;
        }

        public Builder copy() {
            var copied = new Builder(biomeLookup);
            copied.weight = weight;
            copied.yield = yield;
            copied.depletionAmount = depletionAmount;
            copied.depletionChance = depletionChance;
            copied.depletedYield = depletedYield;
            copied.materials = materials;
            return copied;
        }

        public Builder material(Material material, int amount) {
            this.materials.add(Pair.of(material, amount));
            return this;
        }

        public Builder yield(int min, int max) {
            return this.yield(UniformInt.of(min, max));
        }

        public Builder biomes(int weight, TagKey<Biome> biomes) {
            this.biomes.add(new BiomeWeightModifier(biomeLookup.getOrThrow(biomes), weight));
            return this;
        }

        @SafeVarargs
        public final Builder biomes(int weight, ResourceKey<Biome>... biomes) {
            this.biomes.add(new BiomeWeightModifier(HolderSet.direct(biomeLookup::getOrThrow, biomes), weight));
            return this;
        }

        public Builder biomes(int weight, HolderSet<Biome> biomes) {
            this.biomes.add(new BiomeWeightModifier(biomes, weight));
            return this;
        }

        public Builder biomes(List<BiomeWeightModifier> modifiers) {
            this.biomes.addAll(modifiers);
            return this;
        }

        public BedrockOreDefinition build() {
            return new BedrockOreDefinition(weight, size, yield, depletionAmount, depletionChance,
                    depletedYield, materials, BiomeWeightModifier.fromList(biomes), dimensions);
        }
    }
}
