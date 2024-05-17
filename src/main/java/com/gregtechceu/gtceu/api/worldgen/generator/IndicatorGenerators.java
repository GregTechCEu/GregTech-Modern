package com.gregtechceu.gtceu.api.worldgen.generator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.worldgen.generator.indicators.NoopIndicatorGenerator;
import com.gregtechceu.gtceu.api.worldgen.generator.indicators.SurfaceIndicatorGenerator;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

import java.util.function.Function;

public class IndicatorGenerators {

    public static final MapCodec<NoopIndicatorGenerator> NO_OP = register(GTCEu.id("no_op"),
            NoopIndicatorGenerator.CODEC, entry -> NoopIndicatorGenerator.INSTANCE);

    public static final MapCodec<SurfaceIndicatorGenerator> SURFACE = register(GTCEu.id("surface"),
            SurfaceIndicatorGenerator.CODEC, SurfaceIndicatorGenerator::new);

    public static <T extends IndicatorGenerator> MapCodec<T> register(ResourceLocation id, MapCodec<T> codec,
                                                                      Function<GTOreDefinition, T> function) {
        WorldGeneratorUtils.INDICATOR_GENERATORS.put(id, codec);
        WorldGeneratorUtils.INDICATOR_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddons().forEach(IGTAddon::registerIndicatorGenerators);
    }
}
