package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.shader.GTShaders;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "compileChunks",
            at = @At(value = "INVOKE",
                    ordinal = 0,
                    target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;lightOnInSection(Lnet/minecraft/core/SectionPos;)Z"))
    private void gtceu$compileBloomBuffers(Camera camera, CallbackInfo ci,
                                           @Local SectionPos chunkOrigin) {
        BloomUtil.CURRENT_RENDERING_SECTION.set(chunkOrigin);
        BloomUtil.bakeBloomChunkBuffers(chunkOrigin, camera.getPosition());
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void gtceu$resize(int width, int height, CallbackInfo ci) {
        if (GTShaders.BLOOM_CHAIN != null) {
            GTShaders.BLOOM_CHAIN.resize(width, height);
        }
    }

    @Expression("this.renderChunkLayer(tripwire(), ?, ?, ?, ?, ?)")
    @Definition(id = "renderChunkLayer", method = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V")
    @Definition(id = "tripwire", method = "Lnet/minecraft/client/renderer/RenderType;tripwire()Lnet/minecraft/client/renderer/RenderType;")
    @Inject(method = "renderLevel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void gtceu$renderBloom(PoseStack poseStack, float partialTick, long finishNanoTime,
                                   boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                   LightTexture lightTexture, Matrix4f projectionMatrix,
                                   CallbackInfo ci,
                                   @Local Frustum frustum) {
        BloomUtil.renderBloom(camera, (LevelRenderer) (Object) this, poseStack, projectionMatrix, frustum, partialTick);
    }
}
