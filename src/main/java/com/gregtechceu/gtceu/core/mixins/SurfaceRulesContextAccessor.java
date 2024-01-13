package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {
    @Accessor
    RandomState getRandomState();
    @Accessor
    ChunkAccess getChunk();
    @Accessor
    WorldGenerationContext getContext();
    @Accessor
    SurfaceSystem getSystem();
    @Accessor
    NoiseChunk getNoiseChunk();
}
