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

import java.util.IdentityHashMap;
import java.util.Map;

public final class GTEmbeddiumCompat {

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

    public static TerrainRenderPass getBloomRenderPass() {
        return getCustomRenderPass(GTRenderTypes.bloom());
    }

    public static Material getBloomMaterial() {
        return getCustomMaterial(GTRenderTypes.bloom());
    }

    private GTEmbeddiumCompat() {}
}
