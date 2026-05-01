package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode;

import com.gregtechceu.gtceu.client.bloom.BloomSafeMode;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Safe mode version of {@link com.gregtechceu.gtceu.core.mixins.client.bloom.normal.ModelBlockRendererMixin}
 *
 * @see com.gregtechceu.gtceu.core.mixins.client.bloom.normal.ModelBlockRendererMixin
 */
@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @WrapOperation(method = "putQuadData",
                   at = @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V"))
    private void gtceu$copyToBloomBuffer(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                         float[] colorMuls, float red, float green, float blue,
                                         int[] combinedLights, int combinedOverlay, boolean mulColor,
                                         Operation<Void> original) {
        original.call(consumer, pose, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
        BloomSafeMode.copyToBloomBuffer(quad, combinedLights, vertexConsumer -> {
            original.call(vertexConsumer, pose, quad, colorMuls, red, green, blue, combinedLights,
                    combinedOverlay, mulColor);
        });
    }
}
