package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;

public class CoverTextRenderer implements IDynamicCoverRenderer {

    private static final float TEXT_SCALE = 1 / 144f;

    @Setter
    private Supplier<List<? extends Component>> text;

    public CoverTextRenderer(Supplier<List<? extends Component>> text) {
        this.text = text;
    }

    @Override
    public void render(MetaMachine machine, Direction face, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.translate(3 / 16f, 3 / 16f, 0);
        poseStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        int y = 0;
        for (Component s : text.get()) {
            boolean didAnything = false;
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(s, 90)) {
                if (y >= 90) return;
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
