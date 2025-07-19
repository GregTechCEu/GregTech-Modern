package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public class ComputerMonitorCoverRenderer implements IDynamicCoverRenderer {
    @Override
    public void render(MetaMachine machine, Direction face, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float textScale = 1/36f;
        poseStack.translate(3/16f, 3/16f, 0);
        poseStack.scale(textScale, textScale, textScale);
        Minecraft.getInstance().font.drawInBatch(
                "Hello, world!",
                0, 0,
                255,
                false,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                0
        );
    }
}
