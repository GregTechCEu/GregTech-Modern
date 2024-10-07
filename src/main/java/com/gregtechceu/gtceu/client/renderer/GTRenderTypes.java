package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

@OnlyIn(Dist.CLIENT)
public class GTRenderTypes extends RenderType {

    public static final RenderStateShard.OutputStateShard BLOOM_TARGET = new RenderStateShard.OutputStateShard("bloom_target",
            () -> {
                if (GTShaders.allowedShader()) {
                    GTShaders.BLOOM_TARGET.bindWrite(false);
                }
            },
            () -> {
                if (GTShaders.allowedShader()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }
            });

    private static final RenderType LIGHT_RING = RenderType.create("light_ring",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(RenderStateShard.NO_CULL)
                    .setOutputState(BLOOM_TARGET)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false));

    private static final RenderType BLOOM = RenderType.create("gtceu_bloom", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            131072, false, false,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                    .setOutputState(BLOOM_TARGET)
                    .createCompositeState(false));

    private GTRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                          boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getLightRing() {
        return LIGHT_RING;
    }

    public static RenderType getBloom() {
        return BLOOM;
    }
}
