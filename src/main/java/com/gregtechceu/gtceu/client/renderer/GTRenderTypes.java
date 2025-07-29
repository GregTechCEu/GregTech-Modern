package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.Util;
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

    private static final RenderType LIGHT_RING = RenderType.create("light_ring",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
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

    public static RenderType guiTexture(ResourceLocation texture) {
        return GUI_TEXTURE.apply(texture);
    }
}
