package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class NoopVeinGenerator extends VeinGenerator {
    public static final NoopVeinGenerator INSTANCE = new NoopVeinGenerator();
    public static final Codec<NoopVeinGenerator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    public Map<Either<BlockState, Material>, Integer> getAllEntries() {
        return Map.of();
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
        return true;
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

