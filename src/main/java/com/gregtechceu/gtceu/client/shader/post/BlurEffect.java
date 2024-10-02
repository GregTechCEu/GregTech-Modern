package com.gregtechceu.gtceu.client.shader.post;

import com.gregtechceu.gtceu.client.shader.PingPongBuffer;
import com.gregtechceu.gtceu.client.shader.PostTarget;
import com.gregtechceu.gtceu.client.shader.Shaders;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL11;

public class BlurEffect {

    private static PostTarget BLUR_H;
    private static PostTarget BLUR_W;
    private static PostTarget BLUR_H2;
    private static PostTarget BLUR_W2;

    public static void updateSize(int lastWidth, int lastHeight) {
        if (BLUR_H == null) {
            BLUR_H = new PostTarget(lastWidth / 8, lastHeight / 8, false);
            BLUR_H2 = new PostTarget(lastWidth / 4, lastHeight / 4, false);
            BLUR_W = new PostTarget(lastWidth / 8, lastHeight / 8, false);
            BLUR_W2 = new PostTarget(lastWidth / 4, lastHeight / 4, false);
            BLUR_H.setClearColor(0, 0, 0, 0);
            BLUR_H2.setClearColor(0, 0, 0, 0);
            BLUR_W.setClearColor(0, 0, 0, 0);
            BLUR_W2.setClearColor(0, 0, 0, 0);
            BLUR_H.setFilterMode(GL11.GL_LINEAR);
            BLUR_H2.setFilterMode(GL11.GL_LINEAR);
            BLUR_W.setFilterMode(GL11.GL_LINEAR);
            BLUR_W2.setFilterMode(GL11.GL_LINEAR);
        } else if (RenderUtil.updateFBOSize(BLUR_H, lastWidth / 8, lastHeight / 8)) {
            RenderUtil.updateFBOSize(BLUR_H2, lastWidth / 4, lastHeight / 4);
            RenderUtil.updateFBOSize(BLUR_W, lastWidth / 8, lastHeight / 8);
            RenderUtil.updateFBOSize(BLUR_W2, lastWidth / 4, lastHeight / 4);
            BLUR_H.setFilterMode(GL11.GL_LINEAR);
            BLUR_H2.setFilterMode(GL11.GL_LINEAR);
            BLUR_W.setFilterMode(GL11.GL_LINEAR);
            BLUR_W2.setFilterMode(GL11.GL_LINEAR);
        }
        PingPongBuffer.updateSize(lastWidth, lastHeight);
    }

    public static PostTarget renderBlur1(float step) {
        Shaders.renderFullImageInFBO(BLUR_H2, Shaders.BLUR,
                shaderInstance -> shaderInstance.safeGetUniform("blurDir").set(0, step)).bindRead();
        Shaders.renderFullImageInFBO(BLUR_W2, Shaders.BLUR,
                uniformCache -> uniformCache.safeGetUniform("blurDir").set(step, 0)).bindRead();
        Shaders.renderFullImageInFBO(BLUR_H, Shaders.BLUR,
                        uniformCache -> uniformCache.safeGetUniform("blurDir").set(0, step)).bindRead();
        Shaders.renderFullImageInFBO(BLUR_W, Shaders.BLUR,
                        uniformCache -> uniformCache.safeGetUniform("blurDir").set(step, 0)).bindRead();
        return BLUR_W;
    }

    public static RenderTarget renderBlur2(int loop, float step) {
        for (int i = 0; i < loop; i++) {
            Shaders.renderFullImageInFBO(PingPongBuffer.swap(true), Shaders.BLUR,
                    uniformCache -> uniformCache.safeGetUniform("blurDir").set(0, step)).bindRead();
            Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLUR,
                    uniformCache -> uniformCache.safeGetUniform("blurDir").set(step, 0)).bindRead();
        }
        return PingPongBuffer.getCurrentBuffer(false);
    }
}
