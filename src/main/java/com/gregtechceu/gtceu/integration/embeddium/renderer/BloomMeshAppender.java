package com.gregtechceu.gtceu.integration.embeddium.renderer;

import com.gregtechceu.gtceu.client.util.BloomEffectUtil;

import net.minecraft.core.BlockPos;

import org.embeddedt.embeddium.api.MeshAppender;

import java.util.Set;

public class BloomMeshAppender implements MeshAppender {

    public static BloomMeshAppender INSTANCE = new BloomMeshAppender();

    @Override
    public void render(Context context) {
        BlockPos chunkOrigin = context.sectionOrigin().origin();
        BloomEffectUtil.CURRENT_RENDERING_CHUNK_POS.set(chunkOrigin);
        BloomEffectUtil.bakeBloomChunkBuffers(Set.of(chunkOrigin));
    }
}
