package com.gregtechceu.gtceu.api.data.worldgen.ores;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.BulkSectionAccess;

@FunctionalInterface
public interface OreIndicatorPlacer {

    void placeIndicators(BulkSectionAccess access, WorldGenLevel level);
}
