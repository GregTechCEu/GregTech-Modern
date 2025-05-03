package com.gregtechceu.gtceu.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class GTDensityFunctions {

    public static final ResourceKey<DensityFunction> NEW_ORE_VEIN_TOGGLE = create("vein_toggle");
    public static final ResourceKey<DensityFunction> NEW_ORE_VEIN_RIDGED = create("vein_ridged");

    public static void bootstrap(BootstrapContext<DensityFunction> ctx) {
        HolderGetter<NormalNoise.NoiseParameters> noises = ctx.lookup(Registries.NOISE);

        Holder<NormalNoise.NoiseParameters> oreVeininess = noises.getOrThrow(Noises.ORE_VEININESS);
        ctx.register(NEW_ORE_VEIN_TOGGLE,
                DensityFunctions.interpolated(
                        DensityFunctions.noise(oreVeininess, 1.5f, 1.5f)));

        Holder<NormalNoise.NoiseParameters> oreVeinA = noises.getOrThrow(Noises.ORE_VEIN_A);
        Holder<NormalNoise.NoiseParameters> oreVeinB = noises.getOrThrow(Noises.ORE_VEIN_B);
        ctx.register(NEW_ORE_VEIN_RIDGED,
                DensityFunctions.add(
                        DensityFunctions.constant(-0.08f),
                        DensityFunctions.max(
                                DensityFunctions.interpolated(
                                        DensityFunctions.noise(oreVeinA, 4.0f, 4.0f)).abs(),
                                DensityFunctions.interpolated(
                                        DensityFunctions.noise(oreVeinB, 4.0f, 4.0f)).abs())));
    }

    private static ResourceKey<DensityFunction> create(String path) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, GTCEu.id(path));
    }
}
