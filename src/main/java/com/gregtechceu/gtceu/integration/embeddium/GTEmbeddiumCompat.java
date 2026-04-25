package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.integration.embeddium.renderer.BloomMeshAppender;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.embeddedt.embeddium.api.ChunkMeshEvent;

public class GTEmbeddiumCompat {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTEmbeddiumCompat.class);
    }

    @SubscribeEvent
    public static void registerChunkMeshAppenders(ChunkMeshEvent event) {
        if (!GTShaders.canUseBloomShader()) {
            return;
        }

        event.addMeshAppender(BloomMeshAppender.INSTANCE);
    }
}
