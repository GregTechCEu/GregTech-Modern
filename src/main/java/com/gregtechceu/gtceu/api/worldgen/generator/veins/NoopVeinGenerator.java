package com.gregtechceu.gtceu.api.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.worldgen.ores.OreBlockPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;

import java.util.List;
import java.util.Map;

public class NoopVeinGenerator extends VeinGenerator {

    public static final NoopVeinGenerator INSTANCE = new NoopVeinGenerator();
    public static final MapCodec<NoopVeinGenerator> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        return List.of();
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        return Map.of();
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return INSTANCE;
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }
}
