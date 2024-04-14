package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public class NoopVeinGenerator extends VeinGenerator {
    public static final NoopVeinGenerator INSTANCE = new NoopVeinGenerator();
    public static final Codec<NoopVeinGenerator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        return List.of();
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
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
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }
}

