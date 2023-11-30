package com.gregtechceu.gtceu.api.data.worldgen.generator.indicators;

import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreIndicatorPlacer;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Map;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NoopIndicatorGenerator extends IndicatorGenerator {
    public static final NoopIndicatorGenerator INSTANCE = new NoopIndicatorGenerator();
    public static final Codec<NoopIndicatorGenerator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public Map<ChunkPos, OreIndicatorPlacer> generate(WorldGenLevel level, RandomSource random, GeneratedVeinMetadata metadata) {
        // Nothing to do here
        return Collections.emptyMap();
    }

    @Override
    public int getSearchRadiusModifier(int veinRadius) {
        return 0;
    }

    @Override
    public Codec<? extends IndicatorGenerator> codec() {
        return CODEC;
    }
}
