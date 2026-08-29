package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTFeatures {
    // spotless:off

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, GTCEu.MOD_ID);

    public static final DeferredHolder<Feature<?>, StoneBlobFeature> STONE_BLOB = FEATURES.register("stone_blob", StoneBlobFeature::new);
    public static final DeferredHolder<Feature<?>, FluidSproutFeature> FLUID_SPROUT = FEATURES.register("fluid_sprout", FluidSproutFeature::new);

    // spotless:on

    public static void init(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
