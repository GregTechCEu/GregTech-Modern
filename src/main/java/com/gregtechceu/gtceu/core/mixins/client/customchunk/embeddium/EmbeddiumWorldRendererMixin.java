package com.gregtechceu.gtceu.core.mixins.client.customchunk.embeddium;

import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.RenderType;

import com.llamalad7.mixinextras.sugar.Local;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmbeddiumWorldRenderer.class, remap = false)
public class EmbeddiumWorldRendererMixin {

    @Shadow
    private RenderSectionManager renderSectionManager;

    @Inject(method = "drawChunkLayer", at = @At("RETURN"))
    private void gtceu$drawCustomChunkLayer(RenderType renderLayer, Matrix4f normal,
                                            double x, double y, double z, CallbackInfo ci,
                                            @Local(name = "matrices") ChunkRenderMatrices matrices) {
        var pass = GTEmbeddiumCompat.getCustomRenderPass(renderLayer);
        if (pass != null) {
            this.renderSectionManager.renderLayer(matrices, pass, x, y, z);
        }
    }
}
