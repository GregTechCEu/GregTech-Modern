package com.gregtechceu.gtceu.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.worldgen.modifier.DimensionFilter;
import com.gregtechceu.gtceu.api.worldgen.modifier.FrequencyModifier;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTFeatures {

    public static final ResourceLocation NEW_ORE_VEIN_TOGGLE = GTCEu.id("vein_toggle");
    public static final ResourceLocation NEW_ORE_VEIN_RIDGED = GTCEu.id("vein_ridged");

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE,
            GTCEu.MOD_ID);

    public static final DeferredHolder<Feature<?>, StoneBlobFeature> STONE_BLOB = FEATURES.register(
            "stone_blob",
            StoneBlobFeature::new);
    public static final DeferredHolder<Feature<?>, FluidSproutFeature> FLUID_SPROUT = FEATURES.register(
            "fluid_sprout",
            FluidSproutFeature::new);

    private static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;
        inst = RubberTreeChancePlacement.RUBBER_TREE_CHANCE_PLACEMENT;
    }

    public static void register(IEventBus modEventBus) {
        init();
        FEATURES.register(modEventBus);
    }
}
