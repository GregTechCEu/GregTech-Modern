package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public interface IDynamicCoverRenderer {
    void render(MetaMachine machine, Direction face, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
}
