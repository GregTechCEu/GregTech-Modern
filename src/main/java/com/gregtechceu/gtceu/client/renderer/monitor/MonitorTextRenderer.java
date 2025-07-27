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

public class MonitorTextRenderer implements IMonitorRenderer {

    private static final float TEXT_SCALE = 1 / 144f;
    private final List<Component> text;

    public MonitorTextRenderer(List<Component> text) {
        this.text = text;
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockPos rel = group.getRow(0, machine::toRelative).get(0);
        int row = 0;
        int columns = group.getRow(0, machine::toRelative).size();
        poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
        poseStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        int y = 9;
        for (Component s : text) {
            boolean didAnything = false;
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(s, columns * 135)) {
                if (y >= 144) {
                    try {
                        row++;
                        columns = group.getRow(row, machine::toRelative).size();
                        y -= 144;
                        poseStack.translate(-rel.getX() / TEXT_SCALE, -rel.getY() / TEXT_SCALE,
                                -rel.getZ() / TEXT_SCALE);
                        rel = group.getRow(row, machine::toRelative).get(0);
                        poseStack.translate(rel.getX() / TEXT_SCALE, rel.getY() / TEXT_SCALE, rel.getZ() / TEXT_SCALE);
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }
                }
                Minecraft.getInstance().font.drawInBatch(
                        line,
                        9, y,
                        0xFFFFFF,
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
