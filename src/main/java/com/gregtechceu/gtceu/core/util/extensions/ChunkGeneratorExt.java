package com.gregtechceu.gtceu.core.util.extensions;

import com.gregtechceu.gtceu.api.data.worldgen.ores.OrePlacer;

public interface ChunkGeneratorExt {

    default OrePlacer gtceu$getOrePlacer() {
        throw new AssertionError("Mixin didn't apply");
    }
}
