package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPass;
import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.renderer.RenderType;

import lombok.Getter;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
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

    private GTEmbeddiumCompat() {}
}
