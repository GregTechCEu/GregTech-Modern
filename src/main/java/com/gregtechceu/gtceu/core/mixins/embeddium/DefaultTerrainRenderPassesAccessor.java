package com.gregtechceu.gtceu.core.mixins.embeddium;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public interface DefaultTerrainRenderPassesAccessor {

    @Mutable
    @Accessor(value = "ALL")
    static void setAll(TerrainRenderPass[] value) {
        throw new AssertionError();
    }
}
