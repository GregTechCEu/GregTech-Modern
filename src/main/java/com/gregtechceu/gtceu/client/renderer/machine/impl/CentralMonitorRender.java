package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class CentralMonitorRender extends DynamicRender<CentralMonitorMachine, CentralMonitorRender> {

    public static final Codec<CentralMonitorRender> CODEC = Codec.unit(CentralMonitorRender::new);
    public static final DynamicRenderType<CentralMonitorMachine, CentralMonitorRender> TYPE = new DynamicRenderType<>(
            CODEC);

    public CentralMonitorRender() {}

    @Override
    public DynamicRenderType<CentralMonitorMachine, CentralMonitorRender> getType() {
        return TYPE;
    }

    @Override
    public void render(CentralMonitorMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        RenderUtil.moveToFace(poseStack, .5f, .5f, .5f, machine.getFrontFacing());
        RenderUtil.rotateToFace(poseStack, machine.getFrontFacing(), Direction.NORTH);
        poseStack.translate(-machine.getLeftDist() - 1.5f, -machine.getUpDist() - .5f, .01f);
        for (MonitorGroup group : machine.getMonitorGroups()) {
            ItemStack itemStack = group.getItemStackHandler().getStackInSlot(0);
            if (itemStack.getItem() instanceof ComponentItem item) {
                for (IItemComponent component : item.getComponents()) {
                    if (component instanceof IMonitorModuleItem module) {
                        poseStack.pushPose();
                        module.getRenderer(group.getItemStackHandler().getStackInSlot(0), machine, group).render(
                                machine, group,
                                partialTick, poseStack, buffer, packedLight, packedOverlay);
                        poseStack.popPose();
                    }
                }
            }
        }
        float offset = 1 / 16f;
        for (int i = 0; i <= machine.getUpDist() + machine.getDownDist(); i++) {
            for (int j = 0; j <= machine.getLeftDist() + machine.getRightDist(); j++) {
                IMonitorComponent component = machine.getComponent(i, j);
                if (component == null) continue;
                if (component.isMonitor()) {
                    renderRect(
                            poseStack, buffer, packedLight, packedOverlay, 0,
                            machine.isMonitor(i, j - 1) ? j : j + offset,
                            machine.isMonitor(i - 1, j) ? i : i + offset,
                            machine.isMonitor(i, j + 1) ? j + 1 : j + 1 - offset,
                            machine.isMonitor(i + 1, j) ? i + 1 : i + 1 - offset,
                            -.005f);
                }
            }
        }
        poseStack.popPose();
    }

    public void renderRect(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                           int color, float minX, float minY, float maxX, float maxY, float z) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        vertexConsumer.vertex(pose, minX, maxY, z)
                .color(color)
                .uv(0, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();
        vertexConsumer.vertex(pose, maxX, maxY, z)
                .color(color)
                .uv(0, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();
        vertexConsumer.vertex(pose, maxX, minY, z)
                .color(color)
                .uv(0, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();
        vertexConsumer.vertex(pose, minX, minY, z)
                .color(color)
                .uv(0, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, 0, 0, 1)
                .endVertex();

        poseStack.popPose();
    }
}
