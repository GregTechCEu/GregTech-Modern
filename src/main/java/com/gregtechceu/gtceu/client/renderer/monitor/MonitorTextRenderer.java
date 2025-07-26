package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;
import java.util.function.Supplier;

public class MonitorTextRenderer implements IMonitorRenderer {

    private static final float TEXT_SCALE = 1 / 144f;
    private final Supplier<List<Component>> text;

    public MonitorTextRenderer(Supplier<List<Component>> text) {
        this.text = text;
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockPos rel = machine.toRelative(group.getRowLeft(0));
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
        poseStack.translate(1 / 16f, 1 / 16f, 0);
        poseStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        int y = 0;
        for (Component s : text.get()) {
            boolean didAnything = false;
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(s, 140)) {
                if (y >= 140) return;
                Minecraft.getInstance().font.drawInBatch(
                        line,
                        0, y,
                        0x72e500,
                        false,
                        poseStack.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        LightTexture.FULL_BRIGHT);
                y += Minecraft.getInstance().font.lineHeight;
                didAnything = true;
            }
            if (!didAnything) y += Minecraft.getInstance().font.lineHeight;
        }
    }
}
