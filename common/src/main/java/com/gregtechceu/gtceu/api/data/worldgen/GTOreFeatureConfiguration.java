package com.gregtechceu.gtceu.api.data.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOreFeatureConfiguration
 */
public class GTOreFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<GTOreFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTOreFeatureEntry.CODEC
                    .fieldOf("entry")
                    .forGetter(config -> config.entry),
            Codec.floatRange(0.0F, 1.0F)
                    .fieldOf("discard_chance_on_air_exposure")
                    .forGetter(config -> config.discardChanceOnAirExposure),
            Codec.list(OreConfiguration.TargetBlockState.CODEC)
                    .fieldOf("targets")
                    .forGetter(config -> config.targetStates)
    ).apply(instance, GTOreFeatureConfiguration::new));


    protected final GTOreFeatureEntry entry;
    protected final float discardChanceOnAirExposure;
    protected final List<OreConfiguration.TargetBlockState> targetStates;

    public GTOreFeatureConfiguration(GTOreFeatureEntry entry, float discardChanceOnAirExposure, List<OreConfiguration.TargetBlockState> targetStates) {
        this.entry = entry;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.targetStates = targetStates;
    }

}
