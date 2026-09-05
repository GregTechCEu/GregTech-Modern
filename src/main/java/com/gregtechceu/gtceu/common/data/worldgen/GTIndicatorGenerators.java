package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.NoopIndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

public class GTIndicatorGenerators {

    // spotless:off
    private static final DeferredRegister<MapCodec<? extends IndicatorGenerator>> INDICATOR_GENERATORS = DeferredRegister.create(GTRegistries.Keys.INDICATOR_GENERATOR, GTCEu.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IndicatorGenerator>, MapCodec<NoopIndicatorGenerator>> NO_OP = register("no_op", NoopIndicatorGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends IndicatorGenerator>, MapCodec<SurfaceIndicatorGenerator>> SURFACE = register("surface", SurfaceIndicatorGenerator.CODEC);

    private static <T extends IndicatorGenerator> DeferredHolder<MapCodec<? extends IndicatorGenerator>, MapCodec<T>> register(String id, MapCodec<T> codec) {
        return INDICATOR_GENERATORS.register(id, () -> codec);
    }

    //spotless:on
    public static void init(IEventBus modBus) {
        INDICATOR_GENERATORS.register(modBus);
    }
}
