package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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
        BlockPos size = GTUtil.getLast(group.getRow(-1, machine::toRelative)).subtract(rel)
                .subtract(new Vec3i(-1, -1, -1));
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());

        ResourceLocation textureId = ClientImageCache.getOrLoadTexture(url);
        if (textureId == null) return;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(textureId));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        consumer.vertex(matrix, 0, size.getY(), 0).color(0xFFFFFFFF).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(matrix, size.getX(), size.getY(), 0).color(0xFFFFFFFF).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(matrix, size.getX(), 0, 0).color(0xFFFFFFFF).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, 1).endVertex();
        consumer.vertex(matrix, 0, 0, 0).color(0xFFFFFFFF).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, 1).endVertex();
    }
}
