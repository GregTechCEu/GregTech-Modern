package com.gregtechceu.gtceu.client.shader.post;

import com.gregtechceu.gtceu.client.shader.PingPongBuffer;
import com.gregtechceu.gtceu.client.shader.PostTarget;
import com.gregtechceu.gtceu.client.shader.Shaders;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class BloomEffect {

    private static PostTarget[] downSampleFBO;
    private static PostTarget[] upSampleFBO;

    public static float strength = ConfigHolder.INSTANCE.client.shader.strength;
    public static float baseBrightness = ConfigHolder.INSTANCE.client.shader.baseBrightness;
    public static float highBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.highBrightnessThreshold;
    public static float lowBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.lowBrightnessThreshold;
    public static float step = ConfigHolder.INSTANCE.client.shader.step;

    private static void blend(PostTarget bloom, RenderTarget backgroundFBO) {
        // bind main fbo
        backgroundFBO.bindRead();

        // bind blur fbo
        bloom.bindRead();

        // blend shader
        Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLOOM_COMBINE, shaderInstance -> {
            shaderInstance.setSampler("buffer_a", backgroundFBO);
            shaderInstance.setSampler("buffer_b", bloom);
            shaderInstance.safeGetUniform("intensive").set(strength);
            shaderInstance.safeGetUniform("base").set(baseBrightness);
            shaderInstance.safeGetUniform("threshold_up").set(highBrightnessThreshold);
            shaderInstance.safeGetUniform("threshold_down").set(lowBrightnessThreshold);
        });

        RenderSystem.bindTexture(0);
        RenderSystem.setShaderTexture(GL13.GL_TEXTURE1, 0);

        RenderSystem.bindTexture(0);
        RenderSystem.setShaderTexture(GL13.GL_TEXTURE0, 0);

        PingPongBuffer.bindFramebufferTexture();
    }

    private static void cleanUp(int lastWidth, int lastHeight) {
        if (downSampleFBO == null || downSampleFBO.length != ConfigHolder.INSTANCE.client.shader.nMips) {
            if (downSampleFBO != null) {
                for (int i = 0; i < downSampleFBO.length; i++) {
                    downSampleFBO[i].destroyBuffers();
                    upSampleFBO[i].destroyBuffers();
                }
            }

            downSampleFBO = new PostTarget[ConfigHolder.INSTANCE.client.shader.nMips];
            upSampleFBO = new PostTarget[ConfigHolder.INSTANCE.client.shader.nMips];

            int resX = lastWidth / 2;
            int resY = lastHeight / 2;

            for (int i = 0; i < ConfigHolder.INSTANCE.client.shader.nMips; i++) {
                downSampleFBO[i] = new PostTarget(resX, resY, false);
                upSampleFBO[i] = new PostTarget(resX, resY, false);
                downSampleFBO[i].setClearColor(0, 0, 0, 0);
                upSampleFBO[i].setClearColor(0, 0, 0, 0);
                downSampleFBO[i].setFilterMode(GL11.GL_LINEAR);
                upSampleFBO[i].setFilterMode(GL11.GL_LINEAR);
                resX /= 2;
                resY /= 2;
            }
        } else if (RenderUtil.updateFBOSize(downSampleFBO[0], lastWidth / 2, lastHeight / 2)) {
            int resX = lastWidth / 2;
            int resY = lastHeight / 2;
            for (int i = 0; i < ConfigHolder.INSTANCE.client.shader.nMips; i++) {
                RenderUtil.updateFBOSize(downSampleFBO[i], resX, resY);
                RenderUtil.updateFBOSize(upSampleFBO[i], resX, resY);
                downSampleFBO[i].setFilterMode(GL11.GL_LINEAR);
                upSampleFBO[i].setFilterMode(GL11.GL_LINEAR);
                resX /= 2;
                resY /= 2;
            }
        }
        PingPongBuffer.updateSize(lastWidth, lastHeight);
    }

    public static void renderLOG(PostTarget highLightFBO, RenderTarget backgroundFBO) {
        PingPongBuffer.updateSize(backgroundFBO.width, backgroundFBO.height);
        BlurEffect.updateSize(backgroundFBO.width, backgroundFBO.height);
        highLightFBO.bindRead();
        blend(BlurEffect.renderBlur1(step), backgroundFBO);
    }

    public static void renderUnity(PostTarget highLightFBO, RenderTarget backgroundFBO) {
        cleanUp(backgroundFBO.width, backgroundFBO.height);

        renderDownSampling(highLightFBO, downSampleFBO[0]);
        for (int i = 0; i < downSampleFBO.length - 1; i++) {
            renderDownSampling(downSampleFBO[i], downSampleFBO[i + 1]);
        }

        renderUpSampling(downSampleFBO[downSampleFBO.length - 1], downSampleFBO[downSampleFBO.length - 2],
                upSampleFBO[downSampleFBO.length - 2]);
        for (int i = upSampleFBO.length - 2; i > 0; i--) {
            renderUpSampling(upSampleFBO[i], downSampleFBO[i - 1], upSampleFBO[i - 1]);
        }
        renderUpSampling(upSampleFBO[0], highLightFBO, PingPongBuffer.swap());

        RenderSystem.bindTexture(0);
        RenderSystem.setShaderTexture(GL13.GL_TEXTURE1, 0);

        RenderSystem.bindTexture(0);
        RenderSystem.setShaderTexture(GL13.GL_TEXTURE0, 0);

        blend(PingPongBuffer.getCurrentBuffer(false), backgroundFBO);
    }

    private static void renderDownSampling(RenderTarget U, RenderTarget D) {
        U.bindRead();
        Shaders.renderFullImageInFBO(D, Shaders.DOWN_SAMPLING,
                uniformCache -> uniformCache.safeGetUniform("u_resolution2").set(U.width, U.height));
    }

    private static void renderUpSampling(RenderTarget U, RenderTarget D, RenderTarget T) {
        U.bindRead();
        RenderSystem.setShaderTexture(0, U.getColorTextureId());

        D.bindRead();
        RenderSystem.setShaderTexture(1, D.getColorTextureId());

        Shaders.renderFullImageInFBO(T, Shaders.UP_SAMPLING, uniformCache -> {
            uniformCache.setSampler("upTexture", U);
            uniformCache.setSampler("downTexture", D);
            uniformCache.safeGetUniform("u_resolution2").set((float) U.width, (float) U.height);
        });
    }

    public static void renderUnreal(PostTarget highLightFBO, RenderTarget backgroundFBO) {
        cleanUp(backgroundFBO.width, backgroundFBO.height);

        // blur all mips
        int[] kernelSizeArray = new int[] { 3, 5, 7, 9, 11 };
        highLightFBO.bindRead();
        for (int i = 0; i < downSampleFBO.length; i++) {
            RenderTarget buffer_h = downSampleFBO[i];
            int kernel = kernelSizeArray[i];
            Shaders.renderFullImageInFBO(buffer_h, Shaders.S_BLUR, uniformCache -> {
                uniformCache.safeGetUniform("texSize").set((float) buffer_h.width, (float) buffer_h.height);
                uniformCache.safeGetUniform("blurDir").set(step, 0);
                uniformCache.safeGetUniform("kernel_radius").set(kernel);
            }).bindRead();

            RenderTarget buffer_v = upSampleFBO[i];
            Shaders.renderFullImageInFBO(buffer_v, Shaders.S_BLUR, uniformCache -> {
                uniformCache.safeGetUniform("texSize").set((float) buffer_h.width, (float) buffer_h.height);
                uniformCache.safeGetUniform("blurDir").set(0, step);
                uniformCache.safeGetUniform("kernel_radius").set(kernel);
            }).bindRead();
        }

        // composite all mips
        for (int i = 0; i < downSampleFBO.length; i++) {
            upSampleFBO[i].bindRead();
            RenderSystem.setShaderTexture(GL13.GL_TEXTURE0 + i, upSampleFBO[i].getColorTextureId());
        }

        Shaders.renderFullImageInFBO(downSampleFBO[0], Shaders.COMPOSITE, uniformCache -> {
            uniformCache.setSampler("blurTexture1", downSampleFBO[0]);
            uniformCache.setSampler("blurTexture2", downSampleFBO[1]);
            uniformCache.setSampler("blurTexture3", downSampleFBO.length > 2 ? downSampleFBO[2] : null);
            uniformCache.setSampler("blurTexture4", downSampleFBO.length > 3 ? downSampleFBO[3] : null);
            uniformCache.setSampler("blurTexture5", downSampleFBO.length > 4 ? downSampleFBO[4] : null);
            uniformCache.safeGetUniform("bloomStrength").set(strength);
            uniformCache.safeGetUniform("bloomRadius").set(1.0f);
        });

        for (int i = downSampleFBO.length - 1; i >= 0; i--) {
            RenderSystem.setShaderTexture(GL13.GL_TEXTURE0 + i, 0);
        }

        blend(downSampleFBO[0], backgroundFBO);
    }
}
