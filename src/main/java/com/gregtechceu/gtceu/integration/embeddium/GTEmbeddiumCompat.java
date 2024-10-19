package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.integration.embeddium.renderer.BloomMeshAppender;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.embeddedt.embeddium.api.ChunkMeshEvent;

public class GTEmbeddiumCompat {

    @SubscribeEvent
    public static void registerExtraChunkMeshers(ChunkMeshEvent event) {
        event.addMeshAppender(BloomMeshAppender.INSTANCE);
    }
}
