package com.gregtechceu.gtceu.api.data.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/21
 * @implNote GTLayerOreFeatureConfiguration
 */
public class GTLayerOreFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<GTLayerOreFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTOreFeatureEntry.CODEC
                    .fieldOf("entry")
                    .forGetter(config -> config.entry),
            Codec.floatRange(0.0F, 1.0F)
                    .fieldOf("discard_chance_on_air_exposure")
                    .forGetter(config -> config.discardChanceOnAirExposure),
            Codec.list(GTLayerPattern.CODEC)
                    .fieldOf("layer_patterns")
                    .forGetter(config -> config.layerPatterns)
    ).apply(instance, GTLayerOreFeatureConfiguration::new));

    @Getter
    protected final GTOreFeatureEntry entry;
    @Getter
    protected final float discardChanceOnAirExposure;
    @Getter
    protected final List<GTLayerPattern> layerPatterns;

    public GTLayerOreFeatureConfiguration(GTOreFeatureEntry entry, float discardChanceOnAirExposure, List<GTLayerPattern> layerPatterns) {
        this.entry = entry;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.layerPatterns = layerPatterns;
    }

}
