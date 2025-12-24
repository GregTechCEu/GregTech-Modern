package com.gregtechceu.gtceu.core;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IGameRenderer {

    double gtceu$getFov(float partialTicks);

    void gtceu$bob(PoseStack poseStack, float partialTicks);
}
