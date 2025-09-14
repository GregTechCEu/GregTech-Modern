package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Supplier;

public record GraphicsComponent(double x, double y, Supplier<IMonitorRenderer> renderer)
        implements Supplier<IMonitorRenderer> {

    @Override
    public IMonitorRenderer get() {
        return new IMonitorRenderer() {

            private final IMonitorRenderer renderer = GraphicsComponent.this.renderer.get();

            @Override
            public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
                poseStack.pushPose();
                poseStack.translate(x, y, 0);
                this.renderer.render(machine, group, partialTick, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        };
    }
}
