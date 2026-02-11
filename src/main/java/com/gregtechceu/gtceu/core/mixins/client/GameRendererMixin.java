package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.mui.InWorldMUIRenderEvent;
import com.gregtechceu.gtceu.core.IGameRenderer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements IGameRenderer {

    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    protected abstract double getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting);

    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    protected abstract void bobHurt(PoseStack poseStack, float partialTicks);

    @Shadow
    protected abstract void bobView(PoseStack poseStack, float partialTicks);

    @Inject(method = "render",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                     args = "ldc=toasts"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onScreenRender(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci, int i, int j,
                                Window window, Matrix4f matrix4f, PoseStack posestack, GuiGraphics guigraphics) {
        MinecraftForge.EVENT_BUS.post(new InWorldMUIRenderEvent(guigraphics, this.minecraft.getDeltaFrameTime()));
    }

    @Override
    public double gtceu$getFov(float partialTicks) {
        return getFov(mainCamera, partialTicks, true);
    }
}
