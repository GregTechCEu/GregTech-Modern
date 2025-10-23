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

public class QuadPlaceholderRenderer implements IPlaceholderRenderer {

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay, CompoundTag tag) {
        poseStack.pushPose();
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getMonitor());
        Matrix4f pose = poseStack.last().pose();

        consumer.vertex(pose, tag.getFloat("x1"), tag.getFloat("y1"), 0).color(tag.getInt("color1"))
                .uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, tag.getFloat("x2"), tag.getFloat("y2"), 0).color(tag.getInt("color2"))
                .uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, tag.getFloat("x3"), tag.getFloat("y3"), 0).color(tag.getInt("color3"))
                .uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(pose, tag.getFloat("x4"), tag.getFloat("y4"), 0).color(tag.getInt("color4"))
                .uv2(LightTexture.FULL_BRIGHT).endVertex();
        poseStack.popPose();
    }
}
