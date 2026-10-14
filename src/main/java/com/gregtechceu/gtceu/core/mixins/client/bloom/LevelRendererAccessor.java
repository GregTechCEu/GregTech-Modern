package com.gregtechceu.gtceu.core.mixins.client.bloom;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Invoker
    void invokeRenderChunkLayer(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ,
                                Matrix4f projectionMatrix);
}
