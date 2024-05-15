package com.gregtechceu.gtceu.api.worldgen.generator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.worldgen.generator.veins.*;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

import java.util.function.Function;

@SuppressWarnings("unused")
public class VeinGenerators {

    public static final MapCodec<NoopVeinGenerator> NO_OP = register(GTCEu.id("no_op"), NoopVeinGenerator.CODEC,
            entry -> NoopVeinGenerator.INSTANCE);

    public static final MapCodec<StandardVeinGenerator> STANDARD = register(GTCEu.id("standard"),
            StandardVeinGenerator.CODEC, StandardVeinGenerator::new);
    public static final MapCodec<LayeredVeinGenerator> LAYER = register(GTCEu.id("layer"), LayeredVeinGenerator.CODEC,
            LayeredVeinGenerator::new);
    public static final MapCodec<GeodeVeinGenerator> GEODE = register(GTCEu.id("geode"), GeodeVeinGenerator.CODEC,
            GeodeVeinGenerator::new);
    public static final MapCodec<DikeVeinGenerator> DIKE = register(GTCEu.id("dike"), DikeVeinGenerator.CODEC,
            DikeVeinGenerator::new);
    public static final MapCodec<VeinedVeinGenerator> VEINED = register(GTCEu.id("veined"), VeinedVeinGenerator.CODEC,
            VeinedVeinGenerator::new);
    public static final MapCodec<ClassicVeinGenerator> CLASSIC = register(GTCEu.id("classic"),
            ClassicVeinGenerator.CODEC, ClassicVeinGenerator::new);
    public static final MapCodec<CuboidVeinGenerator> CUBOID = register(GTCEu.id("cuboid"), CuboidVeinGenerator.CODEC,
            CuboidVeinGenerator::new);

    public static <
            T extends VeinGenerator> MapCodec<T> register(ResourceLocation id, MapCodec<T> codec,
                                                          Function<GTOreDefinition, ? extends VeinGenerator> function) {
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, function);
        return codec;
    }

    public static void registerAddonGenerators() {
        AddonFinder.getAddons().forEach(IGTAddon::registerVeinGenerators);
    }
}
