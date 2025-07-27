package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

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
        poseStack.popPose();
    }
}
