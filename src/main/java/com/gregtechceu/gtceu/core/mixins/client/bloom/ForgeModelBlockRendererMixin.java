package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.model.BloomMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.lighting.ForgeModelBlockRenderer;
import net.minecraftforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeModelBlockRenderer.class, remap = false)
public class ForgeModelBlockRendererMixin {

    @WrapWithCondition(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/model/lighting/QuadLighter;process(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;I)V"))
    private static boolean gtceu$skipBloomyQuadsFromModel(QuadLighter instance, VertexConsumer consumer,
                                                          PoseStack.Pose pose, BakedQuad quad, int overlay,
                                                          @Local RenderType renderType) {
        if (!GTShaders.canUseBloomShader()) {
            return true;
        }
        int[] combinedLights = gtceu$getAmbientLightmaps((QuadLighterAccessor) instance, quad);

        return BloomMetadataSection.shouldDrawQuad(quad, renderType, combinedLights);
    }

    @Unique
    private static int[] gtceu$getAmbientLightmaps(QuadLighterAccessor lighter, BakedQuad quad) {
        int[] results = new int[4];

        int[] vertices = quad.getVertices();
        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE;
            float xPos = Float.intBitsToFloat(vertices[offset + IQuadTransformer.POSITION]);
            float yPos = Float.intBitsToFloat(vertices[offset + IQuadTransformer.POSITION + 1]);
            float zPos = Float.intBitsToFloat(vertices[offset + IQuadTransformer.POSITION + 2]);

            int packedNormal = vertices[offset + IQuadTransformer.NORMAL];
            byte xNormal = (byte) (packedNormal & 0xFF);
            byte yNormal = (byte) ((packedNormal >> 8) & 0xFF);
            byte zNormal = (byte) ((packedNormal >> 16) & 0xFF);

            float[] adjustedPosition = new float[] {
                    xPos - 0.5f + (xNormal / 127f * 0.5f),
                    yPos - 0.5f + (yNormal / 127f * 0.5f),
                    zPos - 0.5f + (zNormal / 127f * 0.5f)
            };
            results[i] = lighter.callCalculateLightmap(adjustedPosition, new byte[] { xNormal, yNormal, zNormal });
        }

        return results;
    }
}
