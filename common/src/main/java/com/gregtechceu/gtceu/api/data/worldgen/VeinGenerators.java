package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.worldgen.generator.*;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class VeinGenerators {
    public static Codec<NoopVeinGenerator> NO_OP = register(GTCEu.id("no_op"), NoopVeinGenerator.CODEC, entry -> NoopVeinGenerator.INSTANCE);
    public static Codec<StandardVeinGenerator> STANDARD = register(GTCEu.id("standard"), StandardVeinGenerator.CODEC, StandardVeinGenerator::new);
    public static Codec<LayeredVeinGenerator> LAYER = register(GTCEu.id("layer"), LayeredVeinGenerator.CODEC, LayeredVeinGenerator::new);
    public static Codec<GeodeVeinGenerator> GEODE = register(GTCEu.id("geode"), GeodeVeinGenerator.CODEC, GeodeVeinGenerator::new);
    public static Codec<DikeVeinGenerator> DIKE = register(GTCEu.id("dike"), DikeVeinGenerator.CODEC, DikeVeinGenerator::new);

    public static <T extends VeinGenerator> Codec<T> register(ResourceLocation id, Codec<T> codec, Function<GTOreFeatureEntry, ? extends VeinGenerator> function) {
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddons().forEach(IGTAddon::registerVeinGenerators);
    }
}
