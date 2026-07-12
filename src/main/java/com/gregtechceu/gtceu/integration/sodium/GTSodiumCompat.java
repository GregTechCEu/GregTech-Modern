package com.gregtechceu.gtceu.integration.sodium;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.client.sodium.FluidRendererImplAccessor;
import com.gregtechceu.gtceu.utils.TriState;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class GTSodiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = new TerrainRenderPass(GTRenderTypes.bloom(), false, true);
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS, AlphaCutoffParameter.ZERO, true);

    public static boolean quadHasBloom(QuadView quad, int[] ambientPackedLights, boolean emissive) {
        TextureAtlasSprite sprite = SpriteFinderCache.forBlockAtlas().find(quad);
        var metadata = TextureMetadataHelper.getMetadata(sprite);
        if (metadata.isPresent()) {
            TriState bloomValue = metadata.get().bloom();
            if (bloomValue == TriState.TRUE) return true;
            // Explicitly disable bloom if it's set to FALSE in the metadata
            else if (bloomValue == TriState.FALSE) return false;

            // fall through to emissivity config check if default
        }

        if (ConfigHolder.INSTANCE.client.bloom.emissiveTexturesHaveBloom) {
            return emissive || isEmissive(quad, ambientPackedLights);
        }

        return false;
    }

    public static boolean isEmissive(QuadView quad, int[] ambientPackedLights) {
        for (int i = 0; i < 4; i++) {
            int quadLight = quad.lightmap(i);
            int qBlock = LightTexture.block(quadLight), qSky = LightTexture.sky(quadLight);

            int ambientLight = ambientPackedLights[i];
            int aBlock = LightTexture.block(ambientLight), aSky = LightTexture.sky(ambientLight);

            if (qBlock > aBlock || qSky > aSky) {
                return true;
            }
        }
        return false;
    }

    /**
     * Render a fluid state using Sodium's fluid renderer. {@return {@code true} if rendering was successful}
     */
    public static boolean renderFluidBlock(BlockState blockState, FluidState fluidState,
                                           BlockAndTintGetter level, BlockPos blockPos, BlockPos offset) {
        try {
            if (!(level instanceof LevelSlice levelSlice)) {
                return false;
            }
            ChunkBuildContext buildContext = GlobalChunkBuildContext.get();
            if (buildContext == null) {
                return false;
            }

            FluidRenderer fluidRenderer = buildContext.cache.getFluidRenderer();
            if (!(fluidRenderer instanceof FluidRendererImplAccessor accessor)) {
                GTCEu.LOGGER.error(
                        "Sodium's fluid renderer doesn't have our accessor. Maybe it was replaced with a different type?" +
                                "\n            Using slower vanilla fluid renderer implementation.");
                return false;
            }
            TranslucentGeometryCollector collector = accessor.getCurrentDefaultContext().get().gtceu$getCollector();

            fluidRenderer.render(levelSlice, blockState, fluidState, blockPos, offset, collector, buildContext.buffers);
            return true;
        } catch (Exception e) {
            GTCEu.LOGGER.error("Something went wrong with rendering a fluid block using Sodium's fluid renderer.");
            GTCEu.LOGGER.error(e);
            return false;
        }
    }
}
