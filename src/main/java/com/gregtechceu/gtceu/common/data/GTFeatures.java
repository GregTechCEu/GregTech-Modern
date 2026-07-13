package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTFeatures {
    // spotless:off

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, GTCEu.MOD_ID);

    public static final RegistryObject<StoneBlobFeature> STONE_BLOB = FEATURES.register("stone_blob", StoneBlobFeature::new);
    public static final RegistryObject<FluidSproutFeature> FLUID_SPROUT = FEATURES.register("fluid_sprout", FluidSproutFeature::new);

    // spotless:on

    public static void init(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
