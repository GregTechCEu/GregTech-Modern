package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    protected abstract void renderChunkLayer(RenderType renderType, PoseStack poseStack,
                                             double camX, double camY, double camZ, Matrix4f projectionMatrix);

    @Inject(method = "resize", at = @At("TAIL"))
    private void gtceu$resize(int width, int height, CallbackInfo ci) {
        if (GTShaders.BLOOM_CHAIN != null) {
            GTShaders.BLOOM_CHAIN.resize(width, height);
        }
    }

    @Inject(method = "graphicsChanged", at = @At("TAIL"))
    private void gtceu$graphicsChanged(CallbackInfo ci) {
        GTShaders.deinitPostShaders();
    }

    @Definition(id = "renderBuffers", field = "Lnet/minecraft/client/renderer/LevelRenderer;renderBuffers:Lnet/minecraft/client/renderer/RenderBuffers;")
    @Definition(id = "crumblingBufferSource", method = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;")
    @Definition(id = "endBatch", method = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V")
    @Expression("this.renderBuffers.crumblingBufferSource().endBatch()")
    @Inject(method = "renderLevel", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void gtceu$renderBloomBeforeTranslucent(PoseStack poseStack, float partialTick, long finishNanoTime,
                                                    boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                                    LightTexture lightTexture, Matrix4f projectionMatrix,
                                                    CallbackInfo ci,
                                                    @Local Frustum frustum, @Local Vec3 camPos,
                                                    @Local ProfilerFiller profilerFiller) {
        if (!GTShaders.canUseBloomShader()) return;

        profilerFiller.popPush("gtceu:bloom");

        BloomUtil.setupBloomShaderUniforms();

        if (ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom) {
            BloomUtil.setFilterToggleUniform(true);
            this.renderChunkLayer(GTRenderTypes.bloom(), poseStack, camPos.x, camPos.y, camPos.z, projectionMatrix);
        } else {
            BloomUtil.setFilterToggleUniform(false);
        }

        // have to re-setup here. so sad. very aw.
        GTRenderTypes.bloom().setupRenderState();

        BloomUtil.renderSpecialBloom(camera, poseStack, frustum, partialTick, profilerFiller);
        BloomUtil.processPostEffect(partialTick, profilerFiller);

        GTRenderTypes.bloom().clearRenderState();

        // profiler section is popped by popPush() in the calling function; don't pop it here
    }
}
