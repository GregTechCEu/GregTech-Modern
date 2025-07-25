package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;

import net.minecraft.client.renderer.MultiBufferSource;

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
                       int packedLight, int packedOverlay) {}
}
