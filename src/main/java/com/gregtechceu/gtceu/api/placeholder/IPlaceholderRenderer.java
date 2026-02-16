package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IPlaceholderRenderer {

    void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, CompoundTag tag);
}
