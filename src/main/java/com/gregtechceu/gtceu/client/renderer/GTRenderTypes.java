package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.shader.GTShaders;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class GTRenderTypes extends RenderType {

    public static final RenderStateShard.OutputStateShard BLOOM_TARGET = new RenderStateShard.OutputStateShard(
            "bloom_target",
            () -> {
                if (GTShaders.canUseBloomShader()) {
                    GTShaders.BLOOM_TARGET.bindWrite(false);
                }
            },
            () -> {
                if (GTShaders.canUseBloomShader()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }
            });

    private static final RenderType LIGHT_RING = RenderType.create("light_ring", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP, RenderType.SMALL_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(RenderStateShard.NO_CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false));
    private static final RenderType BLOOM = RenderType.create("gtceu:bloom", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, RenderType.BIG_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setOutputState(BLOOM_TARGET)
                    .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> GUI_TEXTURE = Util.memoize((texture) -> {
        return create("gui_texture", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_TEXT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false));
    });

    private GTRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                          boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getLightRing() {
        return LIGHT_RING;
    }

    public static RenderType bloom() {
        return BLOOM;
    }

    public static RenderType guiTexture(ResourceLocation texture) {
        return GUI_TEXTURE.apply(texture);
    }
}
