package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;

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
public abstract class LevelRendererMixin {

    @Inject(method = "resize", at = @At("TAIL"))
    private void gtceu$resizeBloomChain(int width, int height, CallbackInfo ci) {
        if (BloomShaderManager.BLOOM_CHAIN != null) {
            BloomShaderManager.BLOOM_CHAIN.resize(width, height);
        }
    }

    @Inject(method = "deinitTransparency", at = @At("TAIL"))
    private void gtceu$deinitBloomEffect(CallbackInfo ci) {
        BloomShaderManager.deinitPostShaders();
    }

    @Definition(id = "renderBuffers",
                field = "Lnet/minecraft/client/renderer/LevelRenderer;renderBuffers:Lnet/minecraft/client/renderer/RenderBuffers;")
    @Definition(id = "crumblingBufferSource",
                method = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;")
    @Definition(id = "endBatch", method = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V")
    @Expression("this.renderBuffers.crumblingBufferSource().endBatch()")
    @Inject(method = "renderLevel", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void gtceu$renderBloomBeforeTranslucent(PoseStack poseStack, float partialTick, long finishNanoTime,
                                                    boolean renderBlockOutline, Camera camera,
                                                    GameRenderer gameRenderer,
                                                    LightTexture lightTexture, Matrix4f projectionMatrix,
                                                    CallbackInfo ci,
                                                    @Local Frustum frustum,
                                                    @Local ProfilerFiller profilerFiller) {
        BloomUtil.renderBloom(camera, poseStack, frustum, projectionMatrix, partialTick,
                (LevelRenderer) (Object) this, profilerFiller);
    }
}
