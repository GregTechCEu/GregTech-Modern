package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.RenderType;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin {

    @Shadow
    private RenderSectionManager renderSectionManager;

    @Inject(method = "drawChunkLayer", at = @At("RETURN"))
    private void gtceu$drawBloomChunkLayer(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z,
                                           CallbackInfo ci,
                                           @Local(name = "matrices") ChunkRenderMatrices matrices) {
        if (!BloomShaderManager.isBloomActive()) return;

        if (renderLayer == GTRenderTypes.bloom()) {
            this.renderSectionManager.renderLayer(matrices, GTEmbeddiumCompat.BLOOM_RENDER_PASS, x, y, z);
        }
    }
}
