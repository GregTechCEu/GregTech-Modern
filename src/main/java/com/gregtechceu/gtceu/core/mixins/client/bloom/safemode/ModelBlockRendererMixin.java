package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode;

import com.gregtechceu.gtceu.client.bloom.BloomSafeMode;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Safe mode version of {@link com.gregtechceu.gtceu.core.mixins.client.bloom.ModelBlockRendererMixin}
 *
 * @see com.gregtechceu.gtceu.core.mixins.client.bloom.ModelBlockRendererMixin
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
        BloomSafeMode.copyToBloomBuffer(consumer, quad, combinedLights, vertexConsumer -> {
            original.call(vertexConsumer, pose, quad, colorMuls, red, green, blue, colorMuls, colorMuls, mulColor);
        });
    }
}