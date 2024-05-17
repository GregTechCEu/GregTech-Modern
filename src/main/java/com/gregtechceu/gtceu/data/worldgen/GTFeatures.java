package com.gregtechceu.gtceu.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.worldgen.modifier.DimensionFilter;
import com.gregtechceu.gtceu.api.worldgen.modifier.FrequencyModifier;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTFeatures
 */
public class GTFeatures {

    public static final ResourceLocation NEW_ORE_VEIN_TOGGLE = GTCEu.id("vein_toggle");
    public static final ResourceLocation NEW_ORE_VEIN_RIDGED = GTCEu.id("vein_ridged");

    public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE,
            GTCEu.MOD_ID);

    public static final DeferredHolder<Feature<?>, StoneBlobFeature> STONE_BLOB = FEATURE_REGISTER
            .register("stone_blob", StoneBlobFeature::new);
    public static final DeferredHolder<Feature<?>, FluidSproutFeature> FLUID_SPROUT = FEATURE_REGISTER
            .register("fluid_sprout", FluidSproutFeature::new);

    public static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;
    }

    public static void init(IEventBus modEventBus) {
        FEATURE_REGISTER.register(modEventBus);
    }
}
