package com.gregtechceu.gtceu.api.data.worldgen.ores;

import org.jspecify.annotations.NullMarked;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Responsible for actually placing a vein's block.
 * 
 * <p>
 * This is invoked for every block a vein needs to place, when the relevant chunk is being generated.<br>
 * The positions are computed in advance.
 */
@NullMarked
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface OreBlockPlacer {

    /**
     * Place a single block at the (precomputed) location this placer is mapped to.
     */
    void placeBlock(BulkSectionAccess access, LevelChunkSection section);
}
