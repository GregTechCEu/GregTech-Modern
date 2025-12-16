package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class MonitorImageRenderer implements IMonitorRenderer {

    private final String url;

    public MonitorImageRenderer(String url) {
        this.url = url;
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockPos rel = group.getRow(0, machine::toRelative).get(0);
        BlockPos size = GTUtil.getLast(group.getRow(-1, machine::toRelative))
                .offset(-rel.getX() + 1, -rel.getY() + 1, -rel.getZ() + 1);

        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());

        ResourceLocation textureId = ClientImageCache.getOrLoadTexture(url);
        if (textureId == null) return;

        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.guiTexture(textureId));
        Matrix4f pose = poseStack.last().pose();

        float minX = 0, maxX = size.getX();
        float minY = 0, maxY = size.getY();

        consumer.vertex(pose, minX, maxY, 0).color(0xFFFFFFFF).uv(0, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, maxX, maxY, 0).color(0xFFFFFFFF).uv(1, 1).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, maxX, minY, 0).color(0xFFFFFFFF).uv(1, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, minX, minY, 0).color(0xFFFFFFFF).uv(0, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }
}
