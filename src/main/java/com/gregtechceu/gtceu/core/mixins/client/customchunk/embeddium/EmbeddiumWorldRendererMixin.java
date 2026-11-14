package com.gregtechceu.gtceu.core.mixins.client.customchunk.embeddium;

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
public class EmbeddiumWorldRendererMixin {

    @Shadow
    private RenderSectionManager renderSectionManager;

    @Inject(method = "drawChunkLayer", at = @At("RETURN"))
    private void gtceu$drawCustomChunkLayer(RenderType renderLayer, PoseStack poseStack,
                                            double x, double y, double z, CallbackInfo ci,
                                            @Local(name = "matrices") ChunkRenderMatrices matrices) {
        var pass = GTEmbeddiumCompat.getCustomRenderPass(renderLayer);
        if (pass != null) {
            this.renderSectionManager.renderLayer(matrices, pass, x, y, z);
        }
    }
}
