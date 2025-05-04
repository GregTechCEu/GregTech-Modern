package com.gregtechceu.gtceu.api.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class BiomeWeightModifier implements Function<Holder<Biome>, Integer> {

    public static final BiomeWeightModifier EMPTY = new BiomeWeightModifier(HolderSet.empty(), 0);
    // spotless:off
    public static final Codec<BiomeWeightModifier> SINGLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(mod -> mod.biomes),
            Codec.INT.fieldOf("added_weight").forGetter(mod -> mod.addedWeight)
    ).apply(instance, BiomeWeightModifier::new));

    public static final Codec<BiomeWeightModifier> CODEC = Codec
            .lazyInitialized(() -> Codec.withAlternative(FromList.CODEC, SINGLE_CODEC, BiomeWeightModifier::flattenAndWrap))
            .xmap(Function.identity(), BiomeWeightModifier::flattenAndWrap);
    // spotless:on

    public HolderSet<Biome> biomes;
    public int addedWeight;

    public BiomeWeightModifier(HolderSet<Biome> biomes, int addedWeight) {
        this.biomes = biomes;
        this.addedWeight = addedWeight;
    }

    @Override
    public Integer apply(Holder<Biome> biome) {
        if (isEmpty()) {
            return 0;
        }
        return biomes.contains(biome) ? addedWeight : 0;
    }

    public boolean isEmpty() {
        return this == EMPTY || addedWeight <= 0 || biomes.size() == 0;
    }

    public static BiomeWeightModifier fromList(List<BiomeWeightModifier> modifiers) {
        if (modifiers.isEmpty()) {
            return EMPTY;
        }
        List<BiomeWeightModifier> flat = new ArrayList<>();
        for (BiomeWeightModifier inner : modifiers) {
            flat.addAll(flatten(inner));
        }
        return new FromList(flat);
    }

    public static List<BiomeWeightModifier> flatten(BiomeWeightModifier modifier) {
        if (modifier instanceof FromList list) {
            List<BiomeWeightModifier> flat = new ArrayList<>();
            for (BiomeWeightModifier inner : list.originalModifiers) {
                flat.addAll(flatten(inner));
            }
            return flat;
        } else {
            return Collections.singletonList(modifier);
        }
    }

    public static FromList flattenAndWrap(BiomeWeightModifier modifier) {
        return new FromList(flatten(modifier));
    }

    public static class FromList extends BiomeWeightModifier {

        public static final Codec<FromList> CODEC = SINGLE_CODEC.listOf()
                .xmap(FromList::new, FromList::getOriginalModifiers);

        @Getter
        private final List<BiomeWeightModifier> originalModifiers;

        private FromList(List<BiomeWeightModifier> originalModifiers) {
            super(
                    new OrHolderSet<>(originalModifiers.stream().map(mod -> mod.biomes).toList()),
                    originalModifiers.stream().mapToInt(mod -> mod.addedWeight).sum());
            this.originalModifiers = originalModifiers;
        }

        @Override
        public Integer apply(Holder<Biome> biome) {
            int mod = 0;
            for (var modifier : originalModifiers) {
                if (modifier.biomes.contains(biome)) {
                    mod += modifier.apply(biome);
                }
            }
            return mod;
        }

        @Override
        public boolean isEmpty() {
            return originalModifiers.isEmpty();
        }
    }
}
