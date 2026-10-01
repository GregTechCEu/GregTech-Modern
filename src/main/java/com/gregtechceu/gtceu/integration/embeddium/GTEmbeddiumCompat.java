package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPass;
import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import lombok.Getter;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.GlobalChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.FluidRenderer;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import org.embeddedt.embeddium.impl.world.WorldSlice;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public final class GTEmbeddiumCompat {

    private static volatile TerrainRenderPass[] cachedDefaultPasses;
    private static volatile TerrainRenderPass[] cachedCombinedPasses;

    @Getter(lazy = true)
    private static final Map<RenderType, TerrainRenderPass> customRenderPasses = createCustomRenderPasses();
    @Getter(lazy = true)
    private static final Map<RenderType, Material> customMaterials = createCustomMaterials();

    private static Map<RenderType, TerrainRenderPass> createCustomRenderPasses() {
        Map<RenderType, TerrainRenderPass> passes = new IdentityHashMap<>();
        for (var pass : CustomChunkRenderPassRegistry.activePasses()) {
            passes.put(pass.renderType(), new TerrainRenderPass(pass.renderType(), false, true));
        }
        return passes;
    }

    private static Map<RenderType, Material> createCustomMaterials() {
        Map<RenderType, Material> materials = new IdentityHashMap<>();
        for (var pass : CustomChunkRenderPassRegistry.activePasses()) {
            materials.put(pass.renderType(), new Material(getCustomRenderPasses().get(pass.renderType()),
                    getAlphaCutoff(pass.alphaCutoff()), pass.mipped()));
        }
        return materials;
    }

    private static AlphaCutoffParameter getAlphaCutoff(CustomChunkRenderPass.AlphaCutoff alphaCutoff) {
        return switch (alphaCutoff) {
            case ZERO -> AlphaCutoffParameter.ZERO;
            case ONE_TENTH -> AlphaCutoffParameter.ONE_TENTH;
            case HALF -> AlphaCutoffParameter.HALF;
            case ONE -> AlphaCutoffParameter.ONE;
        };
    }

    public static @Nullable TerrainRenderPass getCustomRenderPass(RenderType renderType) {
        return getCustomRenderPasses().get(renderType);
    }

    public static @Nullable Material getCustomMaterial(RenderType renderType) {
        return getCustomMaterials().get(renderType);
    }

    // Extend each backend view instead of mutating Embeddium's static array.
    public static TerrainRenderPass[] includeCustomRenderPasses(TerrainRenderPass[] defaultPasses) {
        TerrainRenderPass[] combinedPasses = cachedCombinedPasses;
        if (defaultPasses == cachedDefaultPasses && combinedPasses != null) {
            return combinedPasses;
        }

        synchronized (GTEmbeddiumCompat.class) {
            if (defaultPasses != cachedDefaultPasses || cachedCombinedPasses == null) {
                cachedCombinedPasses = combineRenderPasses(defaultPasses);
                cachedDefaultPasses = defaultPasses;
            }
            return cachedCombinedPasses;
        }
    }

    private static TerrainRenderPass[] combineRenderPasses(TerrainRenderPass[] defaultPasses) {
        TerrainRenderPass[] customPasses = CustomChunkRenderPassRegistry.activePasses().stream()
                .map(pass -> getCustomRenderPass(pass.renderType()))
                .filter(pass -> Arrays.stream(defaultPasses).noneMatch(existing -> existing == pass))
                .toArray(TerrainRenderPass[]::new);
        if (customPasses.length == 0) return defaultPasses;

        TerrainRenderPass[] passes = Arrays.copyOf(defaultPasses, defaultPasses.length + customPasses.length);
        System.arraycopy(customPasses, 0, passes, defaultPasses.length, customPasses.length);
        return passes;
    }

    public static TerrainRenderPass getBloomRenderPass() {
        return getCustomRenderPass(GTRenderTypes.bloom());
    }

    public static Material getBloomMaterial() {
        return getCustomMaterial(GTRenderTypes.bloom());
    }

    public static TerrainRenderPass getFaceLayerRenderPass() {
        return getCustomRenderPass(GTRenderTypes.faceLayer());
    }

    public static Material getFaceLayerMaterial() {
        return getCustomMaterial(GTRenderTypes.faceLayer());
    }

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

    private GTEmbeddiumCompat() {}
}
