package com.gregtechceu.gtceu.integration.embeddium.renderer;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

import org.embeddedt.embeddium.api.MeshAppender;

public class BloomMeshAppender implements MeshAppender {

    public static BloomMeshAppender INSTANCE = new BloomMeshAppender();

    @Override
    public void render(Context context) {
        SectionPos sectionOrigin = context.sectionOrigin();
        if (!BloomUtil.BLOOM_BUFFER_BUILDERS.containsKey(sectionOrigin)) {
            return;
        }

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BloomUtil.CURRENT_RENDERING_SECTION.set(sectionOrigin);
        BloomUtil.bakeBloomChunkBuffers(sectionOrigin, camPos);
    }
}
