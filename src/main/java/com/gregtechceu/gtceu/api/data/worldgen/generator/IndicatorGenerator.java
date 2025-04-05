package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreIndicatorPlacer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.rhino.util.HideFromJS;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

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

    public static final Codec<IndicatorGenerator> DIRECT_CODEC = REGISTRY_CODEC
            .dispatchStable(IndicatorGenerator::codec, Function.identity());

    protected GTOreDefinition entry;

    public IndicatorGenerator() {}

    public IndicatorGenerator(GTOreDefinition entry) {
        this.entry = entry;
    }

    /**
     * Generate a map of all ore placers (by block position), containing each indicator block for the ore vein.
     *
     * <p>
     * Note that, if in any way possible, this is NOT supposed to directly place any of the indicator blocks, as
     * their respective ore placers are invoked at a later time, when the chunk containing them is actually generated.
     */
    @HideFromJS
    public abstract Map<ChunkPos, OreIndicatorPlacer> generate(WorldGenLevel level, RandomSource random,
                                                               GeneratedVeinMetadata metadata);

    @HideFromJS
    public GTOreDefinition parent() {
        return entry;
    }

    @Nullable
    public abstract Either<BlockState, Material> block();

    public abstract Codec<? extends IndicatorGenerator> codec();

    public abstract int getSearchRadiusModifier(int veinRadius);
}
