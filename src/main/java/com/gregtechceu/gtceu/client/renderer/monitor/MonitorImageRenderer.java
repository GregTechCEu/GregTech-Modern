package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
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
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
        ResourceLocation texture = GTCEu.IMAGE_CACHE.getTexture(url);
        if (texture == null) return;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // this crashes with InvalidStateException: incomplete vertex

        // vertexConsumer.vertex(matrix, 0, 1, 0).color(255, 255, 255, 255).uv(0, 1).uv2(packedLight).normal(normal, 0,
        // 0, 1).overlayCoords(packedOverlay).endVertex();
        // vertexConsumer.vertex(matrix, 1, 1, 0).color(255, 255, 255, 255).uv(1, 1).uv2(packedLight).normal(normal, 0,
        // 0, 1).overlayCoords(packedOverlay).endVertex();
        // vertexConsumer.vertex(matrix, 1, 0, 0).color(255, 255, 255, 255).uv(1, 0).uv2(packedLight).normal(normal, 0,
        // 0, 1).overlayCoords(packedOverlay).endVertex();
        // vertexConsumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).uv2(packedLight).normal(normal, 0,
        // 0, 1).overlayCoords(packedOverlay).endVertex();
    }
}
