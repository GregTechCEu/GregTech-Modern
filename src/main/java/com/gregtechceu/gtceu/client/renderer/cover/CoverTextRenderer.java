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
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class CoverTextRenderer implements IDynamicCoverRenderer {

    private static final float TEXT_SCALE = 1 / 144f;

    @Setter
    private Supplier<List<? extends Component>> text;
    private final DoubleSupplier scale;

    public CoverTextRenderer(Supplier<List<? extends Component>> text, DoubleSupplier scale) {
        this.text = text;
        this.scale = scale;
    }

    @Override
    public void render(MetaMachine machine, Direction face, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.translate(3 / 16f, 3 / 16f, 0);
        float scale = (float) this.scale.getAsDouble();
        poseStack.scale(TEXT_SCALE * scale, TEXT_SCALE * scale, TEXT_SCALE * scale);
        int y = 0;
        for (Component s : text.get()) {
            boolean didAnything = false;
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(s, (int) (90/scale))) {
                if (y >= 90/scale) return;
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
