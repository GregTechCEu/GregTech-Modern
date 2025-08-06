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
    private final float scale;

    public MonitorTextRenderer(List<Component> text, double scale) {
        this.text = text;
        this.scale = (float) scale;
    }

    @Override
    public void render(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        try {
            BlockPos rel = group.getRow(0, machine::toRelative).get(0);
            int row = 0;
            int columns = group.getRow(0, machine::toRelative).size();
            poseStack.translate(rel.getX(), rel.getY(), rel.getZ());
            poseStack.scale(TEXT_SCALE * scale, TEXT_SCALE * scale, TEXT_SCALE * scale);
            float y = 9;
            for (Component s : text) {
                boolean didAnything = false;
                for (FormattedCharSequence line : Minecraft.getInstance().font.split(s,
                        Math.round(columns * 135 / scale))) {
                    if (y >= 144) {
                        try {
                            row++;
                            columns = group.getRow(row, machine::toRelative).size();
                            y -= 144;
                            poseStack.translate(-rel.getX() / (TEXT_SCALE * scale), -rel.getY() / (TEXT_SCALE * scale),
                                    -rel.getZ() / (TEXT_SCALE * scale));
                            rel = group.getRow(row, machine::toRelative).get(0);
                            poseStack.translate(rel.getX() / (TEXT_SCALE * scale), rel.getY() / (TEXT_SCALE * scale),
                                    rel.getZ() / (TEXT_SCALE * scale));
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
                    y += Minecraft.getInstance().font.lineHeight * scale;
                    didAnything = true;
                }
                if (!didAnything) {
                    y += Minecraft.getInstance().font.lineHeight * scale;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }
}
