package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.NoopIndicatorGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class IndicatorGenerators {
    public static final Codec<NoopIndicatorGenerator> NO_OP = register(GTCEu.id("no_op"), NoopIndicatorGenerator.CODEC, entry -> NoopIndicatorGenerator.INSTANCE);

    public static <T extends IndicatorGenerator> Codec<T> register(ResourceLocation id, Codec<T> codec, Function<GTOreDefinition, T> function) {
        WorldGeneratorUtils.INDICATOR_GENERATORS.put(id, codec);
        WorldGeneratorUtils.INDICATOR_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddons().forEach(IGTAddon::registerVeinGenerators);
    }
}
