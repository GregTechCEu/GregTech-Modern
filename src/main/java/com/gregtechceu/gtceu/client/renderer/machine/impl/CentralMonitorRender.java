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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

public class CentralMonitorRender extends DynamicRender<CentralMonitorMachine, CentralMonitorRender> {

    // spotless:off
    public static final Codec<CentralMonitorRender> CODEC = Codec.unit(CentralMonitorRender::new);
    public static final DynamicRenderType<CentralMonitorMachine, CentralMonitorRender> TYPE = new DynamicRenderType<>(CODEC);
    // spotless:on
    private static final float SCREEN_OFFSET_Z = 0.01f;

    public CentralMonitorRender() {}

    @Override
    public DynamicRenderType<CentralMonitorMachine, CentralMonitorRender> getType() {
        return TYPE;
    }

    @Override
    public void render(CentralMonitorMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        RenderUtil.moveToFace(poseStack, 0.5f, 0.5f, 0.5f, machine.getFrontFacing());
        RenderUtil.rotateToFace(poseStack, machine.getFrontFacing(), machine.getUpwardsFacing());
        poseStack.translate(-machine.getRightDist() - 0.5f, -machine.getUpDist() - 0.5f, SCREEN_OFFSET_Z);

        if (machine.getRecipeLogic().isActive()) {
            for (MonitorGroup group : machine.getMonitorGroups()) {
                ItemStack itemStack = group.getItemStackHandler().getStackInSlot(0);
                if (!(itemStack.getItem() instanceof ComponentItem item)) {
                    continue;
                }
                for (IItemComponent component : item.getComponents()) {
                    if (!(component instanceof IMonitorModuleItem module)) {
                        continue;
                    }
                    poseStack.pushPose();
                    module.getRenderer(group.getItemStackHandler().getStackInSlot(0), machine, group)
                            .render(machine, group, partialTick, poseStack, buffer, packedLight, packedOverlay);
                    poseStack.popPose();
                }
            }
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CentralMonitorMachine machine) {
        return true;
    }

    @Override
    public boolean shouldRender(CentralMonitorMachine machine, Vec3 cameraPos) {
        return machine.isFormed();
    }

    @Override
    public AABB getRenderBoundingBox(CentralMonitorMachine machine) {
        BlockPos pos = machine.getPos();
        BoundingBox bounds = new BoundingBox(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

        for (int row = 0; row <= machine.getUpDist() + machine.getDownDist(); row++) {
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IMonitorComponent component = machine.getComponent(row, col);
                if (component != null && component.isMonitor()) {
                    // noinspection deprecation
                    bounds.encapsulate(component.getPos());
                }
            }
        }
        return AABB.of(bounds);
    }
}
