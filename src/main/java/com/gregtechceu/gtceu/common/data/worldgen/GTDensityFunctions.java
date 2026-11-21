package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import static net.minecraft.world.level.levelgen.DensityFunctions.*;

public class GTDensityFunctions {

    // spotless:off
    public static final ResourceKey<DensityFunction> NEW_ORE_VEIN_TOGGLE = ResourceKey.create(Registries.DENSITY_FUNCTION, GTCEu.id("vein_toggle"));
    public static final ResourceKey<DensityFunction> NEW_ORE_VEIN_MASK = ResourceKey.create(Registries.DENSITY_FUNCTION, GTCEu.id("vein_mask"));

    public static void bootstrap(BootstapContext<DensityFunction> ctx) {
        HolderGetter<NormalNoise.NoiseParameters> noises = ctx.lookup(Registries.NOISE);

        Holder<NormalNoise.NoiseParameters> oreVeininess = noises.getOrThrow(Noises.ORE_VEININESS);
        ctx.register(NEW_ORE_VEIN_TOGGLE,
                interpolated(
                        noise(oreVeininess, 1.5f, 1.5f)
                )
        );

        Holder<NormalNoise.NoiseParameters> oreVeinA = noises.getOrThrow(Noises.ORE_VEIN_A);
        Holder<NormalNoise.NoiseParameters> oreVeinB = noises.getOrThrow(Noises.ORE_VEIN_B);
        ctx.register(NEW_ORE_VEIN_MASK,
                add(
                        constant(-0.08f),
                        max(
                                interpolated(noise(oreVeinA, 4.0f, 4.0f))
                                        .abs(),
                                interpolated(noise(oreVeinB, 4.0f, 4.0f))
                                        .abs()
                        )
                )
        );
    }
    // spotless:on
}
