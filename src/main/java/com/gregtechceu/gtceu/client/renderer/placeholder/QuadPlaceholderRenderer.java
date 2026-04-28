package com.gregtechceu.gtceu.client.renderer.placeholder;

import com.gregtechceu.gtceu.api.placeholder.IPlaceholderRenderer;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.LightTexture;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

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

        consumer.addVertex(pose, tag.getFloatOr("x1", 0.0f), tag.getFloatOr("y1", 0.0f), 0)
                .setColor(tag.getIntOr("color1", 0))
                .setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, tag.getFloatOr("x2", 0.0f), tag.getFloatOr("y2", 0.0f), 0)
                .setColor(tag.getIntOr("color2", 0))
                .setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, tag.getFloatOr("x3", 0.0f), tag.getFloatOr("y3", 0.0f), 0)
                .setColor(tag.getIntOr("color3", 0))
                .setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, tag.getFloatOr("x4", 0.0f), tag.getFloatOr("y4", 0.0f), 0)
                .setColor(tag.getIntOr("color4", 0))
                .setLight(LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}
