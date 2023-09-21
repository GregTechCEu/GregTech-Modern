package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.*;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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

    public static final ResourceLocation NEW_ORE_VEIN_TOGGLE = GTCEu.id("vein_toggle");
    public static final ResourceLocation NEW_ORE_VEIN_RIDGED = GTCEu.id("vein_ridged");

    public static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = VeinCountFilter.VEIN_COUNT_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;

        Holder<NormalNoise.NoiseParameters> oreVeininess = BuiltinRegistries.NOISE.getOrCreateHolderOrThrow(Noises.ORE_VEININESS);
        GTRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, NEW_ORE_VEIN_TOGGLE,
                DensityFunctions.interpolated(
                        DensityFunctions.noise(oreVeininess, 1.5f, 1.5f)
                )
        );
        Holder<NormalNoise.NoiseParameters> oreVeinA = BuiltinRegistries.NOISE.getOrCreateHolderOrThrow(Noises.ORE_VEIN_A);
        Holder<NormalNoise.NoiseParameters> oreVeinB = BuiltinRegistries.NOISE.getOrCreateHolderOrThrow(Noises.ORE_VEIN_B);
        GTRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, NEW_ORE_VEIN_RIDGED,
                DensityFunctions.add(
                        DensityFunctions.constant(-0.08f),
                        DensityFunctions.max(
                                DensityFunctions.interpolated(
                                        DensityFunctions.noise(oreVeinA, 4.0f, 4.0f)
                                ).abs(),
                                DensityFunctions.interpolated(
                                        DensityFunctions.noise(oreVeinB, 4.0f, 4.0f)
                                ).abs()
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
