package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTFeatures {

    public static final ResourceLocation NEW_ORE_VEIN_TOGGLE = GTCEu.id("vein_toggle");
    public static final ResourceLocation NEW_ORE_VEIN_RIDGED = GTCEu.id("vein_ridged");

    public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE,
            GTCEu.MOD_ID);

    public static final RegistryObject<StoneBlobFeature> STONE_BLOB = FEATURE_REGISTER.register("stone_blob",
            StoneBlobFeature::new);
    public static final RegistryObject<FluidSproutFeature> FLUID_SPROUT = FEATURE_REGISTER.register("fluid_sprout",
            FluidSproutFeature::new);

    public static void init(IEventBus modEventBus) {
        FEATURE_REGISTER.register(modEventBus);
    }
}
