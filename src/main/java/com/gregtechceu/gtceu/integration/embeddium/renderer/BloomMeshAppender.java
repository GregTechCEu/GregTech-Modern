package com.gregtechceu.gtceu.integration.embeddium.renderer;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;

import org.embeddedt.embeddium.api.MeshAppender;

public class BloomMeshAppender implements MeshAppender {

    public static BloomMeshAppender INSTANCE = new BloomMeshAppender();

    @Override
    public void render(Context context) {
        SectionPos sectionPos = context.sectionOrigin();
        var vertexConsumerProvider = context.vertexConsumerProvider();

        BloomUtil.drawBlockBloomForChunk(sectionPos.asLong(),
                vertexConsumerProvider.apply(GTRenderTypes.bloom()),
                vertexConsumerProvider.apply(RenderType.cutout()));
    }
}
