package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.GlobalChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.FluidRenderer;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import org.embeddedt.embeddium.impl.world.WorldSlice;

public class GTEmbeddiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = new TerrainRenderPass(GTRenderTypes.bloom(), false, true);
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS, AlphaCutoffParameter.ZERO, true);

    /**
     * Render a fluid state using Embeddium's fluid renderer. {@return {@code true} if rendering was successful}
     */
    public static boolean renderFluidBlock(BlockState blockState, FluidState fluidState,
                                           BlockAndTintGetter level, BlockPos blockPos, BlockPos offset) {
        try {
            if (!(level instanceof WorldSlice levelSlice)) {
                return false;
            }
            ChunkBuildContext buildContext = GlobalChunkBuildContext.get();
            if (buildContext == null) {
                return false;
            }

            FluidRenderer fluidRenderer = buildContext.cache.getFluidRenderer();

            fluidRenderer.render(levelSlice, fluidState, blockPos, offset, buildContext.buffers);
            return true;
        } catch (Exception e) {
            GTCEu.LOGGER.error("Something went wrong with rendering a fluid block using Embeddium's fluid renderer.");
            GTCEu.LOGGER.error(e);
            return false;
        }
    }
}
