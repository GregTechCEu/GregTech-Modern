package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.core.IGameRenderer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

    @Override
    public double gtceu$getFov(float partialTicks) {
        return getFov(mainCamera, partialTicks, true);
    }
}
