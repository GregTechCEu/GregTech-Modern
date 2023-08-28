package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Setter;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Screret
 * @date 2023/6/9
 * @implNote GTOreFeatureConfiguration
 */
public class GTOreFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<GTOreFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(GTOreDefinition.CODEC, GTOreDefinition.FULL_CODEC)
                    .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left)
                    .optionalFieldOf("entry", null)
                    .forGetter(config -> config.entry)
        ).apply(instance, GTOreFeatureConfiguration::new)
    );

    @Setter
    private GTOreDefinition entry;


    public GTOreFeatureConfiguration() {
        this.entry = null;
    }

    public GTOreFeatureConfiguration(GTOreDefinition entry) {
        this.entry = entry;
    }

    @Nullable
    public GTOreDefinition getEntry(WorldGenLevel level, Holder<Biome> biome, RandomSource random) {
        if (this.entry != null) return this.entry;
        var veins = WorldGeneratorUtils.getCachedBiomeVeins(level, biome, random);
        int randomEntryIndex = GTUtil.getRandomItem(random, veins, veins.size());
        return randomEntryIndex == -1 ? null : veins.get(randomEntryIndex).getValue();
    }

}
