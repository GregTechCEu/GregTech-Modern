package com.gregtechceu.gtceu.integration.embeddium.renderer;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.shader.GTShaders;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import org.embeddedt.embeddium.api.MeshAppender;

public class BloomMeshAppender implements MeshAppender {

    public static BloomMeshAppender INSTANCE = new BloomMeshAppender();

    @Override
    public void render(Context context) {
        if (!GTShaders.canUseBloomShader()) {
            return;
        }
        BlockPos chunkOrigin = context.sectionOrigin().origin();
        if (!BloomUtil.BLOOM_BUFFER_BUILDERS.containsKey(chunkOrigin)) {
            return;
        }

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BloomUtil.CURRENT_RENDERING_CHUNK_POS.set(chunkOrigin);
        BloomUtil.bakeBloomChunkBuffers(chunkOrigin, camPos);
    }
}
