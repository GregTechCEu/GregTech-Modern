package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.integration.embeddium.renderer.BloomMeshAppender;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import org.embeddedt.embeddium.api.ChunkMeshEvent;

public class GTEmbeddiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = TerrainRenderPass.builder()
            .layer(GTRenderTypes.bloom())
            .fragmentDiscard(true)
            .build();
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS,
            AlphaCutoffParameter.ONE_TENTH, false);


    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTEmbeddiumCompat.class);
    }

    @SubscribeEvent
    public static void registerChunkMeshAppenders(ChunkMeshEvent event) {
        if (!GTShaders.canUseBloomShader()) return;
        if (!BloomUtil.chunkSectionHasBloomQuads(event.getSectionOrigin().asLong())) {
            return;
        }

        event.addMeshAppender(BloomMeshAppender.INSTANCE);
    }
}
