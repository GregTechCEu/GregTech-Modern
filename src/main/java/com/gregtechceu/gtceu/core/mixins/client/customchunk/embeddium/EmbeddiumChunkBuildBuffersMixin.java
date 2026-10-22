package com.gregtechceu.gtceu.core.mixins.client.customchunk.embeddium;

import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkBuildBuffers.class, remap = false)
public class EmbeddiumChunkBuildBuffersMixin {

    @ModifyExpressionValue(method = "<init>",
                           at = @At(value = "FIELD",
                                    opcode = Opcodes.GETSTATIC,
                                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/DefaultTerrainRenderPasses;ALL:[Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;"))
    private TerrainRenderPass[] gtceu$includeCustomRenderPasses(TerrainRenderPass[] defaultPasses) {
        return GTEmbeddiumCompat.includeCustomRenderPasses(defaultPasses);
    }
}
