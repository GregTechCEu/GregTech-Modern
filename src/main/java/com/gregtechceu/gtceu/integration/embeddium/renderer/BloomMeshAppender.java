package com.gregtechceu.gtceu.integration.embeddium.renderer;

import com.gregtechceu.gtceu.client.util.BloomEffectUtil;

import net.minecraft.core.BlockPos;

import org.embeddedt.embeddium.api.MeshAppender;

public class BloomMeshAppender implements MeshAppender {

    public static BloomMeshAppender INSTANCE = new BloomMeshAppender();

    @Override
    public void render(Context context) {
        BlockPos chunkOrigin = context.sectionOrigin().origin();
        BloomEffectUtil.bakeBloomChunkBuffers(chunkOrigin);
    }
}
