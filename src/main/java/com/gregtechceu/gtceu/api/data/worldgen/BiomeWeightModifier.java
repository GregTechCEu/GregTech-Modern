package com.gregtechceu.gtceu.api.data.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Function;
import java.util.function.Supplier;

public class BiomeWeightModifier implements Function<Holder<Biome>, Integer> {
    public static final Codec<BiomeWeightModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(mod -> mod.biomes.get()),
                    Codec.INT.fieldOf("added_weight").forGetter(mod -> mod.addedWeight)
            ).apply(instance, (biomes, weight) -> new BiomeWeightModifier(() -> biomes, weight))
    );

    public Supplier<HolderSet<Biome>> biomes;
    public int addedWeight;

    public BiomeWeightModifier(Supplier<HolderSet<Biome>> biomes, int addedWeight) {
        this.biomes = biomes;
        this.addedWeight = addedWeight;
    }

    @Override
    public Integer apply(Holder<Biome> biome) {
        return biomes.get().contains(biome) ? addedWeight : 0;
    }
}
