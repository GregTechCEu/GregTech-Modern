package com.gregtechceu.gtceu.api.data.worldgen.generator.indicators;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinPosition;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class IndicatorGenerator {
    /**
     * Generate vein indicators inside the current chunk.
     */
    @HideFromJS
    public abstract void generate(WorldGenLevel level, RandomSource random, GeneratedVeinPosition veinPosition, ChunkPos currentChunk);
}
