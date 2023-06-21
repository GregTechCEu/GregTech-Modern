package com.gregtechceu.gtceu.api.data.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Function;

public class BiomeWeightModifier implements Function<Holder<Biome>, Integer> {
    public static final Codec<BiomeWeightModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(mod -> mod.biomes),
                    Codec.INT.fieldOf("added_weight").forGetter(mod -> mod.addedWeight)
            ).apply(instance, BiomeWeightModifier::new)
    );

    public HolderSet<Biome> biomes;
    public int addedWeight;

    public BiomeWeightModifier(HolderSet<Biome> biomes, int addedWeight) {
        this.biomes = biomes;
        this.addedWeight = addedWeight;
    }

    @Override
    public Integer apply(Holder<Biome> biome) {
        return biomes.contains(biome) ? addedWeight : 0;
    }
}
