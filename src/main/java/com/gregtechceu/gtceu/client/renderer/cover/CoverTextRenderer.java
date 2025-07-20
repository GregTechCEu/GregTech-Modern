package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.Supplier;

public class CoverTextRenderer implements IDynamicCoverRenderer {
    @Setter
    private Supplier<String> text;

    public CoverTextRenderer(Supplier<String> text) {
        this.text = text;
    }
    @Override
    public void render(MetaMachine machine, Direction face, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float textScale = 1/144f;
        poseStack.translate(3/16f, 3/16f, 0);
        poseStack.scale(textScale, textScale, textScale);
        int y = 0;
        for (String s : text.get().split("\\n")) {
            if (s.isEmpty()) y += Minecraft.getInstance().font.lineHeight;
            for (FormattedCharSequence line : Minecraft.getInstance().font.split(FormattedText.of(s), 90)) {
                Minecraft.getInstance().font.drawInBatch(
                        line,
                        0, y,
                        0x72e500,
                        false,
                        poseStack.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        LightTexture.FULL_BRIGHT
                );
                y += Minecraft.getInstance().font.lineHeight;
            }
        }

    }
}
