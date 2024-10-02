package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.client.shader.PostTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.*;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {

    public static boolean updateFBOSize(PostTarget fbo, int width, int height) {
        if (fbo.width != width || fbo.height != height) {
            fbo.createBuffers(width, height, Minecraft.ON_OSX);
            return true;
        }
        return false;
    }

    public static void hookDepthBuffer(RenderTarget fbo, int depthBuffer) {
        // Hook DepthBuffer
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                depthBuffer, 0);
        if (fbo.isStencilEnabled()) {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D,
                    depthBuffer, 0);
        }
    }

    public static void hookDepthTexture(RenderTarget fbo, int depthTexture) {
        // Hook DepthTexture
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        if (fbo.isStencilEnabled()) {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                    GL11.GL_TEXTURE_2D, depthTexture, 0);
        } else {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL11.GL_TEXTURE_2D, depthTexture, 0);
        }
    }
}
