package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

import java.util.Collections;
import java.util.List;

public class NoopVeinGenerator extends VeinGenerator {

    public static final NoopVeinGenerator INSTANCE = new NoopVeinGenerator();
    public static final Codec<NoopVeinGenerator> CODEC = Codec.unit(INSTANCE);

    @Override
    public List<VeinEntry> getAllEntries() {
        return Collections.emptyList();
    }

    @Override
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        return Long2ObjectMaps.emptyMap();
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
