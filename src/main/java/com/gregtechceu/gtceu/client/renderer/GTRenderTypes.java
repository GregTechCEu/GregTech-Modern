package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class GTRenderTypes extends RenderType {

    public static final RenderStateShard.OutputStateShard BLOOM_TARGET = new OutputStateShard(
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
    // spotless:off
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_BLOOM_SHADER = new ShaderStateShard(BloomShaderManager::getRendertypeBloomShader);
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_BLOOM_SHADER = new ShaderStateShard(BloomShaderManager::getRendertypeEntityBloomShader);
    public static final RenderStateShard.ShaderStateShard POSITION_TEX_COLOR_SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);
    // spotless:on

    private static final RenderType LIGHT_RING = RenderType.create("light_ring",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP,
            RenderType.SMALL_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(POSITION_COLOR_SHADER)
                    .createCompositeState(false));

    private static final RenderType BLOOM = RenderType.create("gtceu:bloom",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.BIG_BUFFER_SIZE, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_BLOOM_SHADER)
                    .setOutputState(BLOOM_TARGET)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .createCompositeState(true));

    private static final RenderType FACADE_SOLID = RenderType.create("gtceu:facade_solid",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.BIG_BUFFER_SIZE, true, false,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_SOLID_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true));
    private static final RenderType FACADE_CUTOUT_MIPPED = RenderType.create("gtceu:facade_cutout_mipped",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.BIG_BUFFER_SIZE, true, false,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_CUTOUT_MIPPED_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true));
    private static final RenderType FACADE_CUTOUT = RenderType.create("gtceu:facade_cutout",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.SMALL_BUFFER_SIZE, true, false,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_CUTOUT_SHADER)
                    .setTextureState(BLOCK_SHEET)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true));
    private static final RenderType FACADE_TRANSLUCENT = RenderType.create("gtceu:facade_translucent",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.SMALL_BUFFER_SIZE, true, true,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true));
    private static final RenderType FACADE_TRIPWIRE = RenderType.create("gtceu:facade_tripwire",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            RenderType.TRANSIENT_BUFFER_SIZE, true, true,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(RENDERTYPE_TRIPWIRE_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(WEATHER_TARGET)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true));
    private static final Function<ResourceLocation, RenderType> ENTITY_BLOOM = Util.memoize((texture) -> {
        return create("gtceu:entity_bloom",
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_BLOOM_SHADER)
                        .setOutputState(BLOOM_TARGET)
                        .setWriteMaskState(COLOR_WRITE)
                        .setLayeringState(POLYGON_OFFSET_LAYERING)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setTextureState(new TextureStateShard(texture, false, true))
                        .createCompositeState(true));
    });

    private static final RenderType MONITOR = RenderType.create("central_monitor",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
            RenderType.TRANSIENT_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false));

    private static final RenderType ASSEMBLY_LINE = RenderType.create("assembly_line",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS,
            RenderType.TRANSIENT_BUFFER_SIZE, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(CULL)
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> GUI_TEXTURE_TRIANGLE_STRIP = Util.memoize((texture) -> {
        return create("gui_texture_triangle_strip", DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_TEX_COLOR_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
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

    public static RenderType facade(RenderType source) {
        if (source == RenderType.solid()) return FACADE_SOLID;
        if (source == RenderType.cutoutMipped()) return FACADE_CUTOUT_MIPPED;
        if (source == RenderType.cutout()) return FACADE_CUTOUT;
        if (source == RenderType.translucent()) return FACADE_TRANSLUCENT;
        if (source == RenderType.tripwire()) return FACADE_TRIPWIRE;
        throw new IllegalArgumentException("Unsupported facade render type: " + source);
    }

    public static RenderType entityBloom(ResourceLocation location) {
        return ENTITY_BLOOM.apply(location);
    }

    @SuppressWarnings("deprecation")
    public static RenderType entityBloomBlockSheet() {
        return entityBloom(TextureAtlas.LOCATION_BLOCKS);
    }

    public static RenderType assemblyLine() {
        return ASSEMBLY_LINE;
    }

    public static RenderType getMonitor() {
        return MONITOR;
    }

    public static RenderType guiTriangleStrip(ResourceLocation texture) {
        return GUI_TEXTURE_TRIANGLE_STRIP.apply(texture);
    }
}
