package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NormalNoise.class)
public interface NormalNoiseAccessor {

    @Accessor
    double getValueFactor();

    @Accessor
    PerlinNoise getFirst();

    @Accessor
    PerlinNoise getSecond();
}
