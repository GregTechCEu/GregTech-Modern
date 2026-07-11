package com.gregtechceu.gtceu.core.mixins.client.bloom;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Invoker
    void invokeRenderSectionLayer(RenderType renderType, double camX, double camY, double camZ,
                                  Matrix4f frustumMatrix, Matrix4f projectionMatrix);
}
