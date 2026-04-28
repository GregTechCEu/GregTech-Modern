package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public final class GTBlockRenderLayers {

    private GTBlockRenderLayers() {}

    public static ChunkSectionLayer solid() {
        return ChunkSectionLayer.SOLID;
    }

    public static ChunkSectionLayer cutout() {
        return ChunkSectionLayer.CUTOUT;
    }

    public static ChunkSectionLayer cutoutMipped() {
        return ChunkSectionLayer.CUTOUT;
    }

    public static ChunkSectionLayer translucent() {
        return ChunkSectionLayer.TRANSLUCENT;
    }
}
