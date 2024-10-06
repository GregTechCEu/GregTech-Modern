package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.model.GTMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.lighting.QuadLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuadLighter.class, remap = false)
public class QuadLighterMixin {

    @WrapOperation(method = "process",
                   at = @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V",
                            remap = true)
    )
    private void gtceu$swapEmissiveBuffer(VertexConsumer instance, PoseStack.Pose poseEntry, BakedQuad quad,
                                          float[] colorMuls, float red, float green, float blue,
                                          int[] combinedLights, int combinedOverlay, boolean mulColor,
                                          Operation<Void> original) {
        // Check if quad is full brightness OR we have bloom enabled for the quad
        if (GTShaders.allowedShader() && (!quad.isShade() || GTMetadataSection.hasBloom(quad.getSprite()))) {
            original.call(GTShaders.BLOOM_BUFFER, poseEntry, quad,
                    colorMuls, red, green, blue,
                    combinedLights, combinedOverlay, mulColor);
        }
        original.call(instance, poseEntry, quad,
                colorMuls, red, green, blue,
                combinedLights, combinedOverlay, mulColor);
    }
}
