package com.gregtechceu.gtceu.core.mixins.xaerominimap;

import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;

import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.IXaeroMinimap;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.common.minimap.render.MinimapRenderer;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.compass.render.CompassRenderer;
import xaero.hud.minimap.element.render.over.MinimapElementOverMapRendererHandler;
import xaero.hud.minimap.waypoint.render.WaypointsGuiRenderer;

@Mixin(value = MinimapRenderer.class, remap = false)
public abstract class MinimapRendererMixin {

    @Unique
    private int gtceu$frameSize;
    @Unique
    private GenericMapRenderer gtceu$renderer;
    @Unique
    private float gtceu$angle;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtceu$injectConstruct(IXaeroMinimap modMain, Minecraft mc, WaypointsGuiRenderer waypointsGuiRenderer,
                                       Minimap minimapInterface, CompassRenderer compassRenderer,
                                       CallbackInfo ci) {
        gtceu$renderer = new XaerosRenderer();
    }

    // these capture* methods are to avoid having to use a particularly horrible and volatile local capture to get some
    // values
    // note: this one seems to cause mcDev to freak out in weird ways, try commenting and uncommenting the
    // @ModifyVariable to "fix" random "errors" that pop up
    // if veins suddenly only render on the minimap right next to where you are, you forgot to uncomment it
    @ModifyVariable(method = "renderMinimap", at = @At(value = "LOAD", ordinal = 0), name = "minimapFrameSize")
    private int gtceu$captureMinimapSize(int frameSize) {
        this.gtceu$frameSize = frameSize;
        return frameSize;
    }

    @WrapOperation(method = "renderMinimap", at = @At(value = "INVOKE", target = "Ljava/lang/Math;toRadians(D)D"))
    private double gtceu$captureMinimapAngle(double v, Operation<Double> original) {
        gtceu$angle = (float) v - 90;
        return original.call(v);
    }

    @WrapOperation(method = "renderMinimap",
                   at = @At(value = "INVOKE",
                            target = "Lxaero/hud/minimap/element/render/over/MinimapElementOverMapRendererHandler;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;DDDDZFLcom/mojang/blaze3d/pipeline/RenderTarget;Lxaero/common/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)V"))
    private void gtceu$injectRender(MinimapElementOverMapRendererHandler instance, GuiGraphics guiGraphics,
                                    Entity renderEntity, Player player,
                                    Vec3 renderPos, double playerDimDiv, double ps, double pc, double zoom, boolean cave,
                                    float partialTicks, RenderTarget frameBuffer,
                                    MultiTextureRenderTypeRendererProvider provider, Operation<Void> original) {
        gtceu$renderer.updateVisibleArea(player.level().dimension(), (int) (renderPos.x - gtceu$frameSize),
                (int) (renderPos.z - gtceu$frameSize), gtceu$frameSize * 2, gtceu$frameSize * 2);

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_GREATER);
        poseStack.mulPose(new Quaternionf(gtceu$angle, 0, 0, 1));
        poseStack.scale((float) zoom, (float) zoom, 1);
        poseStack.translate(-renderPos.x, -renderPos.z, 0);
        gtceu$renderer.render(guiGraphics, renderPos.x, renderPos.z, (float) zoom);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.disableDepthTest();
        poseStack.popPose();

        original.call(instance, guiGraphics, renderEntity, player, renderPos,
                playerDimDiv, ps, pc, zoom, cave, partialTicks, frameBuffer, provider);
    }

    @WrapOperation(method = "renderMinimap",
                   at = @At(value = "INVOKE",
                            target = "Lxaero/common/minimap/render/MinimapRendererHelper;drawMyTexturedModalRect(Lcom/mojang/blaze3d/vertex/PoseStack;FFIIFFFF)V"))
    private void gtceu$depthRectMinimap(MinimapRendererHelper instance, PoseStack matrixStack, float x, float y,
                                        int textureX, int textureY, float width, float height, float textureHeight,
                                        float factor, Operation<Void> original) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.colorMask(false, false, false, false);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 1000);
        instance.drawMyTexturedModalRect(matrixStack, x, y, textureX, textureY, width, height, textureHeight, factor);
        matrixStack.popPose();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.disableDepthTest();

        original.call(instance, matrixStack, x, y, textureX, textureY, width, height, textureHeight, factor);
    }

    @WrapOperation(method = "renderMinimap",
                   at = @At(value = "INVOKE",
                            target = "Lxaero/common/minimap/render/MinimapRendererHelper;drawTexturedElipseInsideRectangle(Lcom/mojang/blaze3d/vertex/PoseStack;DIFFIIFF)V"))
    private void gtceu$depthCircleMinimap(MinimapRendererHelper instance, PoseStack matrixStack,
                                          double startAngle, int sides, float x, float y, int textureX, int textureY,
                                          float width, float widthFactor, Operation<Void> original) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.colorMask(false, false, false, false);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 1000);
        original.call(instance, matrixStack, startAngle, sides, x, y, textureX, textureY, width, widthFactor);
        matrixStack.popPose();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.disableDepthTest();

        original.call(instance, matrixStack, startAngle, sides, x, y, textureX, textureY, width, widthFactor);
    }
}
