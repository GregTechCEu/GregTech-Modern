package com.gregtechceu.gtceu.api.data.worldgen.generator.indicators;

import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinPosition;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NoopIndicatorGenerator extends IndicatorGenerator {
    public static final NoopIndicatorGenerator INSTANCE = new NoopIndicatorGenerator();
    public static final Codec<NoopIndicatorGenerator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public void generate(WorldGenLevel level, RandomSource random, GeneratedVeinPosition veinPosition, ChunkPos currentChunk) {
        // Nothing to do here
    }

    @Override
    public Codec<? extends IndicatorGenerator> codec() {
        return CODEC;
    }
}
