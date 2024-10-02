package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author KilaBash
 * @date 2021/09/11
 * @implNote You'll need it when you need to get deep textures to do more cool things.
 *           The default FBO is used, unfortunately, sometimes we have to abandon native way to create a new fbo.
 *           But generally not.
 */
@OnlyIn(Dist.CLIENT)
public class DepthTextureUtil {

    public static int framebufferObject;
    public static int framebufferDepthTexture;
    private static boolean useDefaultFBO = true;
    private static boolean lastBind;
    private static int lastWidth, lastHeight;

    private static boolean shouldRenderDepthTexture() {
        return lastBind && !GTCEu.isOptifineLoaded() && ConfigHolder.INSTANCE.client.hookDepthTexture;
    }

    public static void onPreWorldRender(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START && mc.level != null) {
            if (shouldRenderDepthTexture()) {
                if (useDefaultFBO && GL11.glGetError() != 0) { // if we can't use the vanilla fbo.... okay, why not
                                                               // create our own fbo?
                    useDefaultFBO = false;
                    if (framebufferDepthTexture != 0) {
                        disposeDepthTexture();
                        createDepthTexture();
                    }
                }
                if (framebufferDepthTexture == 0) {
                    createDepthTexture();
                } else if (lastWidth != mc.getMainRenderTarget().width ||
                        lastHeight != mc.getMainRenderTarget().height) {
                            disposeDepthTexture();
                            createDepthTexture();
                        }
            } else {
                disposeDepthTexture();
            }
            lastBind = false;
        }
    }

    public static void renderWorld(RenderLevelStageEvent event) { // re-render world in our own fbo.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Entity viewer = mc.getCameraEntity();
        if (DepthTextureUtil.framebufferDepthTexture != 0 && mc.level != null && viewer != null &&
                !DepthTextureUtil.useDefaultFBO) {
            int lastFBO = GlStateManager.getBoundFramebuffer();
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferObject);
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            var projectionMatrix = RenderSystem.getProjectionMatrix();
            mc.levelRenderer.renderChunkLayer(RenderType.solid(), event.getPoseStack(),
                    viewer.getX(), viewer.getY(), viewer.getZ(), projectionMatrix);
            mc.levelRenderer.renderChunkLayer(RenderType.solid(), event.getPoseStack(),
                    viewer.getX(), viewer.getY(), viewer.getZ(), projectionMatrix);
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFBO);
        }
    }

    public static void createDepthTexture() {
        int lastFBO = GlStateManager._getInteger(GL30.GL_FRAMEBUFFER_BINDING);
        RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
        boolean stencil = framebuffer.isStencilEnabled() && useDefaultFBO;

        if (useDefaultFBO) {
            framebufferObject = framebuffer.frameBufferId;
        } else {
            framebufferObject = GlStateManager.glGenFramebuffers();
        }

        framebufferDepthTexture = TextureUtil.generateTextureId(); // gen texture
        RenderSystem.bindTexture(framebufferDepthTexture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0,
                stencil ? GL30.GL_DEPTH24_STENCIL8 : GL14.GL_DEPTH_COMPONENT24,
                framebuffer.viewWidth,
                framebuffer.viewHeight, 0,
                stencil ? GL30.GL_DEPTH_STENCIL : GL11.GL_DEPTH_COMPONENT,
                stencil ? GL30.GL_UNSIGNED_INT_24_8 : GL11.GL_UNSIGNED_INT, null);
        RenderSystem.bindTexture(0);

        lastWidth = framebuffer.viewWidth;
        lastHeight = framebuffer.viewHeight;

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferObject); // bind buffer then bind depth texture
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : GL30.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D,
                framebufferDepthTexture, 0);

        if (BloomEffectUtil.getBloomFBO() != null && useDefaultFBO) {
            RenderUtil.hookDepthTexture(BloomEffectUtil.getBloomFBO(), framebufferDepthTexture);
        }

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFBO);
    }

    public static void disposeDepthTexture() {
        if (framebufferDepthTexture != 0 || framebufferObject != 0) {
            if (useDefaultFBO) {
                RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
                RenderTarget bloomFBO = BloomEffectUtil.getBloomFBO();
                if (bloomFBO != null) {
                    RenderUtil.hookDepthBuffer(bloomFBO, framebuffer.getDepthTextureId());
                }
                RenderUtil.hookDepthBuffer(framebuffer, framebuffer.getDepthTextureId());
            } else {
                RenderSystem.glDeleteBuffers(framebufferObject);
            }
            RenderSystem.deleteTexture(framebufferDepthTexture);
            framebufferObject = 0;
            framebufferDepthTexture = 0;
        }
    }

    public static void bindDepthTexture() {
        lastBind = true;
        if (useDefaultFBO && framebufferDepthTexture != 0) {
            RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
            RenderUtil.hookDepthBuffer(mainTarget, mainTarget.getDepthTextureId());
        }
        RenderSystem.bindTexture(framebufferDepthTexture);
    }

    public static void unBindDepthTexture() {
        RenderSystem.bindTexture(0);
        if (useDefaultFBO) {
            RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
            RenderUtil.hookDepthTexture(framebuffer, framebufferDepthTexture);
        }
    }

    public static boolean isUseDefaultFBO() {
        return useDefaultFBO;
    }

    public static boolean isLastBind() {
        return framebufferObject != 0;
    }
}
