package com.gregtechceu.gtceu.api.worldgen.ores;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.chunk.BulkSectionAccess;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface OreIndicatorPlacer {

    void placeIndicators(BulkSectionAccess access);
}
