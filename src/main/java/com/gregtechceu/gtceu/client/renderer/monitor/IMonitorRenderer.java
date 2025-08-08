package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IMonitorRenderer {

    @OnlyIn(Dist.CLIENT)
    void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay);
}
