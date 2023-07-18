package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class VeinGenerators {
    public static Codec<GTOreFeatureEntry.NoopVeinGenerator> NO_OP = register(GTCEu.id("no_op"), GTOreFeatureEntry.NoopVeinGenerator.CODEC, entry -> GTOreFeatureEntry.NoopVeinGenerator.INSTANCE);
    public static Codec<GTOreFeatureEntry.StandardVeinGenerator> STANDARD = register(GTCEu.id("standard"), GTOreFeatureEntry.StandardVeinGenerator.CODEC, GTOreFeatureEntry.StandardVeinGenerator::new);
    public static Codec<GTOreFeatureEntry.LayeredVeinGenerator> LAYER = register(GTCEu.id("layer"), GTOreFeatureEntry.LayeredVeinGenerator.CODEC, GTOreFeatureEntry.LayeredVeinGenerator::new);

    public static <T extends GTOreFeatureEntry.VeinGenerator> Codec<T> register(ResourceLocation id, Codec<T> codec, Function<GTOreFeatureEntry, ? extends GTOreFeatureEntry.VeinGenerator> function) {
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddons().forEach(IGTAddon::registerVeinGenerators);
    }
}
