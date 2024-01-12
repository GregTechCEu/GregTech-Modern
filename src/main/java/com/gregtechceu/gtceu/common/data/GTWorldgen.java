package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.worldgen.strata.BlobStrata;
import com.gregtechceu.gtceu.common.worldgen.strata.LayerStrata;
import com.gregtechceu.gtceu.common.worldgen.strata.StrataPicker;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import static com.gregtechceu.gtceu.common.data.GTFeatures.*;

public class GTWorldgen {

    public static void bootstrapDensityFunctions(BootstapContext<DensityFunction> ctx) {
        HolderGetter<NormalNoise.NoiseParameters> noises = ctx.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> densityFunctions = ctx.lookup(Registries.DENSITY_FUNCTION);

        Holder<NormalNoise.NoiseParameters> oreVeininess = noises.getOrThrow(Noises.ORE_VEININESS);
        ctx.register(ResourceKey.create(Registries.DENSITY_FUNCTION, NEW_ORE_VEIN_TOGGLE),
                DensityFunctions.interpolated(
                        DensityFunctions.noise(oreVeininess, 1.5f, 1.5f)
                )
        );

        Holder<NormalNoise.NoiseParameters> oreVeinA = noises.getOrThrow(Noises.ORE_VEIN_A);
        Holder<NormalNoise.NoiseParameters> oreVeinB = noises.getOrThrow(Noises.ORE_VEIN_B);
        ctx.register(ResourceKey.create(Registries.DENSITY_FUNCTION, NEW_ORE_VEIN_RIDGED),
                DensityFunctions.add(
                        DensityFunctions.constant(-0.08f),
                        DensityFunctions.max(
                                DensityFunctions.interpolated(
                                        DensityFunctions.noise(oreVeinA, 4.0f, 4.0f)
                                ).abs(),
                                DensityFunctions.interpolated(
                                        DensityFunctions.noise(oreVeinB, 4.0f, 4.0f)
                                ).abs()
                        )
                )
        );

        Holder<NormalNoise.NoiseParameters> strataNoiseHolder = noises.getOrThrow(STRATA_NOISE);
        DensityFunction shiftX = new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(NoiseRouterData.SHIFT_X));
        DensityFunction shiftZ = new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(NoiseRouterData.SHIFT_Z));
        ctx.register(BASE_3D_STRATA_NOISE, DensityFunctions.cacheAllInCell(
            new DensityFunctions.ShiftedNoise(
                shiftX,
                BlendedNoise.createUnseeded(0.75, 3, 180, 16, 1),
                shiftZ,
                2, 3,
                new DensityFunction.NoiseHolder(strataNoiseHolder)
            )
        ));
    }

    public static void bootstrapNoises(BootstapContext<NormalNoise.NoiseParameters> ctx) {
        ctx.register(STRATA_NOISE, new NormalNoise.NoiseParameters(-9, 1.0, 1.0, 0.0, 10.0));
        ctx.register(STRATA_TYPE_NOISE, new NormalNoise.NoiseParameters(-5, 1.0, 5.0, 10.0, 0.0, 1.0));
    }

    public static void bootstrapRules(BootstapContext<Codec<? extends SurfaceRules.RuleSource>> ctx) {
        ctx.register(BLOB_STRATA, BlobStrata.CODEC.codec());
        ctx.register(LAYER_STRATA, LayerStrata.CODEC.codec());
        ctx.register(STRATA, StrataPicker.CODEC.codec());
    }
}
