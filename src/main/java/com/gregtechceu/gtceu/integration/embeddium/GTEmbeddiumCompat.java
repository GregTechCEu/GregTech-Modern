package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;

public class GTEmbeddiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = TerrainRenderPass.builder()
            .layer(GTRenderTypes.bloom())
            .fragmentDiscard(true)
            .build();
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS,
            AlphaCutoffParameter.ONE_TENTH, false);

    public static void init() {}
}
