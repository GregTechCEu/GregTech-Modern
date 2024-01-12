package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.DimensionFilter;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.FrequencyModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.worldgen.strata.BlobStrata;
import com.gregtechceu.gtceu.common.worldgen.strata.LayerStrata;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTFeatures
 */
public class GTFeatures {
    public static final ResourceLocation NEW_ORE_VEIN_TOGGLE = GTCEu.id("vein_toggle");
    public static final ResourceLocation NEW_ORE_VEIN_RIDGED = GTCEu.id("vein_ridged");

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTER = DeferredRegister.create(Registries.CONFIGURED_FEATURE, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTER = DeferredRegister.create(Registries.PLACED_FEATURE, GTCEu.MOD_ID);
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.MOD_ID);

    public static final ResourceKey<NormalNoise.NoiseParameters> STRATA_NOISE = ResourceKey.create(Registries.NOISE, GTCEu.id("strata"));
    public static final ResourceKey<NormalNoise.NoiseParameters> STRATA_TYPE_NOISE = ResourceKey.create(Registries.NOISE, GTCEu.id("strata_type"));
    public static final ResourceKey<DensityFunction> BASE_3D_STRATA_NOISE = ResourceKey.create(Registries.DENSITY_FUNCTION, GTCEu.id("strata"));

    public static final ResourceKey<Codec<? extends SurfaceRules.RuleSource>> BLOB_STRATA = ResourceKey.create(Registries.MATERIAL_RULE, GTCEu.id("blob_strata"));
    public static final ResourceKey<Codec<? extends SurfaceRules.RuleSource>> LAYER_STRATA = ResourceKey.create(Registries.MATERIAL_RULE, GTCEu.id("layer_strata"));

    public static final ResourceKey<WorldPreset> STRATA_PRESET = ResourceKey.create(Registries.WORLD_PRESET, GTCEu.id("strata"));
    public static final ResourceKey<NoiseGeneratorSettings> BLOB_STRATA_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, GTCEu.id("blob_strata"));
    public static final ResourceKey<NoiseGeneratorSettings> LAYER_STRATA_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, GTCEu.id("layer_strata"));

    public static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;

        GTRegistries.register(BuiltInRegistries.MATERIAL_RULE, BLOB_STRATA.location(), BlobStrata.CODEC.codec());
        GTRegistries.register(BuiltInRegistries.MATERIAL_RULE, LAYER_STRATA.location(), LayerStrata.CODEC.codec());
    }

    public static void init(IEventBus modEventBus) {
        CONFIGURED_FEATURE_REGISTER.register(modEventBus);
        PLACED_FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_REGISTER.register(modEventBus);
    }

    public static void register() {
        // no-op
    }
}
