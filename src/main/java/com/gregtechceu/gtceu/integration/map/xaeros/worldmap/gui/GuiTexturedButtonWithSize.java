package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;
import xaero.map.gui.CursorBox;
import xaero.map.gui.GuiTexturedButton;

import java.util.function.Supplier;

public class GuiTexturedButtonWithSize extends GuiTexturedButton {

    protected int spriteW;
    protected int spriteH;

    public GuiTexturedButtonWithSize(int x, int y, int w, int h, int textureX, int textureY, int textureW, int textureH,
                                     int spriteW, int spriteH, ResourceLocation texture, OnPress onPress,
                                     Supplier<CursorBox> tooltip) {
        super(x, y, w, h, textureX, textureY, textureW, textureH, texture, onPress, tooltip);
        this.spriteW = spriteW;
        this.spriteH = spriteH;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int iconX = this.getX() + this.width / 2 - this.textureW / 2;
        int iconY = this.getY() + this.height / 2 - this.textureH / 2;
        if (this.active) {
            if (this.isHovered) {
                --iconY;
                RenderSystem.setShaderColor(0.9F, 0.9F, 0.9F, 1.0F);
            } else {
                RenderSystem.setShaderColor(0.9882F, 0.9882F, 0.9882F, 1.0F);
            }
        } else {
            RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        }

        if (this.isFocused()) {
            guiGraphics.fill(iconX, iconY, iconX + this.textureW, iconY + this.textureH, 1442840575);
        }

        // this whole override is just to be able to pass a texture size here.
        guiGraphics.blit(this.texture, iconX, iconY, this.textureX, this.textureY, this.textureW, this.textureH,
                spriteW, spriteH);
    }
}
