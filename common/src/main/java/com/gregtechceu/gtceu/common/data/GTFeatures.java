package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.generator.*;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTFeatures
 */
public class GTFeatures {
    public static final GTOreFeature ORE = GTRegistries.register(BuiltInRegistries.FEATURE, GTCEu.id("ore"), new GTOreFeature());
//    public static final ConfiguredFeature<GTOreFeatureConfiguration, GTOreFeature> CONFIGURED_ORE = GTRegistries.register(Registries.CONFIGURED_FEATURE, GTCEu.id("ore"), new ConfiguredFeature<>(ORE, new GTOreFeatureConfiguration()));
//    public static final PlacedFeature PLACED_ORE = GTRegistries.register(BuiltinRegistries.PLACED_FEATURE, GTCEu.id("ore"), new PlacedFeature(BuiltinRegistries.CONFIGURED_FEATURE.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTCEu.id("ore"))), List.of(InSquarePlacement.spread())));

    public static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = BiomeFilter.BIOME_FILTER;
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = VeinCountFilter.VEIN_COUNT_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;
        register();
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

}
