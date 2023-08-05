package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.*;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.api.data.worldgen.strata.StrataGenerationType;
import com.gregtechceu.gtceu.api.data.worldgen.vein.GTOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.vein.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

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

    public static final ResourceKey<NormalNoise.NoiseParameters> STRATA_NOISE = ResourceKey.create(Registry.NOISE_REGISTRY, GTCEu.id("strata"));
    public static final ResourceKey<NormalNoise.NoiseParameters> STRATA_TYPE_NOISE = ResourceKey.create(Registry.NOISE_REGISTRY, GTCEu.id("strata_type"));
    public static final ResourceKey<DensityFunction> BASE_3D_STRATA_NOISE = ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, GTCEu.id("strata"));

    public static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = BiomeFilter.BIOME_FILTER;
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = VeinCountFilter.VEIN_COUNT_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;

        GTRegistries.register(BuiltinRegistries.NOISE, STRATA_NOISE.location(), new NormalNoise.NoiseParameters(-9, 1.0, 1.0, 0.0, 10.0));
        GTRegistries.register(BuiltinRegistries.NOISE, STRATA_TYPE_NOISE.location(), new NormalNoise.NoiseParameters(-5, 1.0, 5.0, 10.0, 0.0, 1.0));
        GTRegistries.register(Registry.RULE, GTCEu.id("blob_strata"), IStrataLayer.BlobStrata.CODEC.codec());
        GTRegistries.register(Registry.RULE, GTCEu.id("layer_strata"), IStrataLayer.LayerStrata.CODEC.codec());
        Holder<NormalNoise.NoiseParameters> strataNoiseHolder = BuiltinRegistries.NOISE.getOrCreateHolderOrThrow(STRATA_NOISE);
        GTRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, BASE_3D_STRATA_NOISE.location(),
                DensityFunctions.cache2d(
                        DensityFunctions.mul(
                                new DensityFunctions.ShiftedNoise(
                                        BuiltinRegistries.DENSITY_FUNCTION.get(NoiseRouterData.SHIFT_X),
                                        BlendedNoise.createUnseeded(0.75, 3, 180, 16, 1),
                                        BuiltinRegistries.DENSITY_FUNCTION.get(NoiseRouterData.SHIFT_Z),
                                        0.5, 3,
                                        new DensityFunction.NoiseHolder(strataNoiseHolder)
                                ),
                                DensityFunctions.constant(-1)
                        )
                )
        );

        register();
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

}
