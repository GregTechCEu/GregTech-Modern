package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinPosition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class IndicatorGenerator {
    public static final Codec<Codec<? extends IndicatorGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC
            .flatXmap(rl -> Optional.ofNullable(WorldGeneratorUtils.INDICATOR_GENERATORS.get(rl))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "No IndicatorGenerator with id " + rl + " registered")),
                    obj -> Optional.ofNullable(WorldGeneratorUtils.INDICATOR_GENERATORS.inverse().get(obj))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "IndicatorGenerator " + obj + " not registered")));

    public static final Codec<IndicatorGenerator> DIRECT_CODEC = REGISTRY_CODEC.dispatchStable(IndicatorGenerator::codec, Function.identity());

    /**
     * Generate vein indicators inside the current chunk.
     */
    @HideFromJS
    public abstract void generate(WorldGenLevel level, RandomSource random, GeneratedVeinPosition veinPosition, ChunkPos currentChunk);

    public abstract Codec<? extends IndicatorGenerator> codec();
}
