package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
                if (BloomShaderManager.isBloomActive()) {
                    BloomShaderManager.BLOOM_TARGET.bindWrite(false);
                }
            },
            () -> {
                if (BloomShaderManager.isBloomActive()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }
            });
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_BLOOM_SHADER = new RenderStateShard.ShaderStateShard(
            BloomShaderManager::getRendertypeBloomShader);
    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_BLOOM_SHADER = new RenderStateShard.ShaderStateShard(
            BloomShaderManager::getRendertypeEntityBloomShader);

    private static final RenderType LIGHT_RING = RenderType.create("light_ring", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP, RenderType.SMALL_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(POSITION_COLOR_SHADER)
                    .createCompositeState(false));

    private static final RenderType BLOOM = RenderType.create("gtceu:bloom", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, RenderType.BIG_BUFFER_SIZE, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_BLOOM_SHADER)
                    .setOutputState(BLOOM_TARGET)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .createCompositeState(true));
    private static final Function<ResourceLocation, RenderType> ENTITY_BLOOM = Util.memoize((texture) -> {
        return create("gtceu:entity_bloom", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_BLOOM_SHADER)
                        .setOutputState(BLOOM_TARGET)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .createCompositeState(true));
    });

    private static final RenderType MONITOR = RenderType.create("central_monitor",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, RenderType.TRANSIENT_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
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

    public static RenderType lightRing() {
        return LIGHT_RING;
    }

    public static RenderType bloom() {
        return BLOOM;
    }

    public static RenderType entityBloom(ResourceLocation location) {
        return ENTITY_BLOOM.apply(location);
    }

    @SuppressWarnings("deprecation")
    public static RenderType entityBloomBlockSheet() {
        return entityBloom(TextureAtlas.LOCATION_BLOCKS);
    }

    public static RenderType getMonitor() {
        return MONITOR;
    }

    public static RenderType guiTexture(ResourceLocation texture) {
        return GUI_TEXTURE.apply(texture);
    }
}
