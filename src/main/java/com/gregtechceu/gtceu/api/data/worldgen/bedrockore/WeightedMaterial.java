package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.utils.WeightedEntry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeightedMaterial(Material material, int weight) implements WeightedEntry {

    public static final Codec<WeightedMaterial> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(GTCEuAPI.materialManager.codec().fieldOf("material").forGetter(v -> v.material),
                    Codec.INT.fieldOf("weight").forGetter(v -> v.weight))
                    .apply(instance, WeightedMaterial::new));
}
