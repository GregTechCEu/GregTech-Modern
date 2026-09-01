package com.gregtechceu.gtceu.core.mixins.client.customchunk.sodium;

import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkBuildBuffers.class, remap = false)
public class SodiumChunkBuildBuffersMixin {

    @ModifyExpressionValue(method = "<init>",
                           at = @At(value = "FIELD",
                                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/DefaultTerrainRenderPasses;ALL:[Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;"))
    private TerrainRenderPass[] gtceu$includeCustomRenderPasses(TerrainRenderPass[] defaultPasses) {
        return GTSodiumCompat.includeCustomRenderPasses(defaultPasses);
    }
}
