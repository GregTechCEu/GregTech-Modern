package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

@OnlyIn(Dist.CLIENT)
public class GTRenderTypes extends RenderType {

    public static final RenderStateShard.ShaderStateShard BLOOM_SHADER = new RenderStateShard.ShaderStateShard(GTShaders::getBloomShader);
    protected static final RenderStateShard.OutputStateShard BLOOM_TARGET = new RenderStateShard.OutputStateShard("bloom_target",
            () -> GTShaders.BLOOM_TARGET.bindWrite(false),
            () -> GTShaders.BLOOM_TARGET.bindWrite(false));

    private static final RenderType LIGHT_RING = RenderType.create("light_ring",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(RenderStateShard.NO_CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false));

    private static final RenderType BLOOM = RenderType.create("gtceu_bloom", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(BLOOM_SHADER)
                    .setOutputState(BLOOM_TARGET)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
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
