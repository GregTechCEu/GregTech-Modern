package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.generator.FrequencyModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomeFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.DimensionFilter;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTFeatures
 */
public class GTFeatures {
    public static final GTOreFeature ORE = GTRegistries.register(Registry.FEATURE, GTCEu.id("ore"), new GTOreFeature());

    public static final ConfiguredFeature<GTOreFeatureConfiguration, GTOreFeature> CONFIGURED_ORE = GTRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE, GTCEu.id("ore"), new ConfiguredFeature<>(ORE, new GTOreFeatureConfiguration()));
    public static final PlacedFeature PLACED_ORE = GTRegistries.register(BuiltinRegistries.PLACED_FEATURE, GTCEu.id("ore"), new PlacedFeature(BuiltinRegistries.CONFIGURED_FEATURE.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTCEu.id("ore"))), List.of(InSquarePlacement.spread())));

    public static void init() {
        Object inst = FrequencyModifier.Frequency_PLACEMENT; // seemingly useless access to init the class in time
        inst = BiomeFilter.BIOME_FILTER;
        inst = DimensionFilter.DIMENSION_FILTER;
        register();
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

}
