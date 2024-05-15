package com.gregtechceu.gtceu.common.worldgen.feature.configurations;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StoneBlobConfiguration(OreConfiguration.TargetBlockState state, IntProvider size)
        implements FeatureConfiguration {

    public static final Codec<StoneBlobConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    OreConfiguration.TargetBlockState.CODEC.fieldOf("state").forGetter(StoneBlobConfiguration::state),
                    IntProvider.codec(1, 64).fieldOf("size").forGetter(StoneBlobConfiguration::size))
                    .apply(instance, StoneBlobConfiguration::new));
}
