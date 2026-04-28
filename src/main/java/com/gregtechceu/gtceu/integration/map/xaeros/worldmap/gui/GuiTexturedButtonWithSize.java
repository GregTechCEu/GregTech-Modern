package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.gui;

import com.gregtechceu.gtceu.integration.map.GTMapIds;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import xaero.map.gui.CursorBox;
import xaero.map.gui.GuiTexturedButton;

import java.util.function.Supplier;

public class GuiTexturedButtonWithSize extends GuiTexturedButton {

    protected int spriteW;
    protected int spriteH;

    public GuiTexturedButtonWithSize(int x, int y, int w, int h, int textureX, int textureY, int textureW, int textureH,
                                     int spriteW, int spriteH, Identifier texture, OnPress onPress,
                                     Supplier<CursorBox> tooltip) {
        super(x, y, w, h, textureX, textureY, textureW, textureH, GTMapIds.toResourceLocation(texture), onPress,
                tooltip);
        this.spriteW = spriteW;
        this.spriteH = spriteH;
    }

    @Override
    public void renderWidget(GuiGraphics GuiGraphics, int mouseX, int mouseY, float partialTick) {
        int iconX = this.getX() + this.width / 2 - this.textureW / 2;
        int iconY = this.getY() + this.height / 2 - this.textureH / 2;
        int color;
        if (this.active) {
            if (this.isHovered) {
                --iconY;
                color = ARGB.colorFromFloat(1.0F, 0.9F, 0.9F, 0.9F);
            } else {
                color = ARGB.colorFromFloat(1.0F, 0.9882F, 0.9882F, 0.9882F);
            }
        } else {
            color = ARGB.colorFromFloat(1.0F, 0.25F, 0.25F, 0.25F);
        }

        if (this.isFocused()) {
            GuiGraphics.fill(iconX, iconY, iconX + this.textureW, iconY + this.textureH, 1442840575);
        }

        // this whole override is just to be able to pass a texture size here.
        GuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture.toIdentifier(), iconX, iconY, this.textureX,
                this.textureY, this.textureW, this.textureH, spriteW, spriteH, color);
    }
}
