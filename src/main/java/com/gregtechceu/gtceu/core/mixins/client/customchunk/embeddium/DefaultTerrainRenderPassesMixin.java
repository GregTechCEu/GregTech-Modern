package com.gregtechceu.gtceu.core.mixins.client.customchunk.embeddium;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import org.apache.commons.lang3.ArrayUtils;
import org.embeddedt.embeddium.impl.render.chunk.terrain.DefaultTerrainRenderPasses;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {

    @Shadow
    @Final
    @Mutable
    public static TerrainRenderPass[] ALL;

    static {
        TerrainRenderPass[] customPasses = CustomChunkRenderPassRegistry.activePasses().stream()
                .map(pass -> GTEmbeddiumCompat.getCustomRenderPass(pass.renderType()))
                .toArray(TerrainRenderPass[]::new);
        ALL = ArrayUtils.addAll(ALL, customPasses);
    }
}
