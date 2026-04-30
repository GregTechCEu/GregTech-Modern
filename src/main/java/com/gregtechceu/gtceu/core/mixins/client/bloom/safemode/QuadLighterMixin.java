package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode;

import com.gregtechceu.gtceu.client.bloom.BloomSafeMode;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Safe mode version of {@link com.gregtechceu.gtceu.core.mixins.client.bloom.QuadLighterMixin}
 *
 * @see com.gregtechceu.gtceu.core.mixins.client.bloom.QuadLighterMixin
 */
@Mixin(value = QuadLighter.class, remap = false)
public class QuadLighterMixin {

    @WrapOperation(method = "process",
                   at = @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V",
                            remap = true))
    private void gtceu$copyToBloomBuffer(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                         float[] colorMuls, float red, float green, float blue,
                                         int[] combinedLights, int combinedOverlay, boolean mulColor,
                                         Operation<Void> original) {
        BloomSafeMode.copyToBloomBuffer(consumer, quad, combinedLights, vertexConsumer -> {
            original.call(vertexConsumer, pose, quad, colorMuls, red, green, blue, colorMuls, colorMuls, mulColor);
        });
    }
}
