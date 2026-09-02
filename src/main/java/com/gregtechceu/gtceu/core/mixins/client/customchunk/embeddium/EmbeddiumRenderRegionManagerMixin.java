package com.gregtechceu.gtceu.core.mixins.client.customchunk.embeddium;

import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegionManager;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RenderRegionManager.class, remap = false)
public class EmbeddiumRenderRegionManagerMixin {

    @ModifyExpressionValue(method = "uploadMeshes(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/render/chunk/region/RenderRegion;Ljava/util/Collection;)V",
                           at = @At(value = "FIELD",
                                    opcode = Opcodes.GETSTATIC,
                                    target = "Lorg/embeddedt/embeddium/impl/render/chunk/terrain/DefaultTerrainRenderPasses;ALL:[Lorg/embeddedt/embeddium/impl/render/chunk/terrain/TerrainRenderPass;"))
    private TerrainRenderPass[] gtceu$includeCustomRenderPasses(TerrainRenderPass[] defaultPasses) {
        return GTEmbeddiumCompat.includeCustomRenderPasses(defaultPasses);
    }
}
