package com.gregtechceu.gtceu.client.renderer.placeholder;

import com.gregtechceu.gtceu.api.placeholder.IPlaceholderRenderer;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class RectPlaceholderRenderer implements IPlaceholderRenderer {

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay, CompoundTag tag) {
        poseStack.pushPose();
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getMonitor());
        Matrix4f pose = poseStack.last().pose();
        float minX = 0, maxX = tag.getFloat("width");
        float minY = 0, maxY = tag.getFloat("height");
        int color = tag.getInt("color");

        consumer.vertex(pose, minX, maxY, 0).color(color).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, maxX, maxY, 0).color(color).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, maxX, minY, 0).color(color).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, minX, minY, 0).color(color).uv2(LightTexture.FULL_BRIGHT).endVertex();
        poseStack.popPose();
    }
}
