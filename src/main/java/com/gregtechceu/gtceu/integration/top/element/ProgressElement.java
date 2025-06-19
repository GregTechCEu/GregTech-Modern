package com.gregtechceu.gtceu.integration.top.element;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import mcjty.theoneprobe.rendering.RenderHelper;
import org.jetbrains.annotations.Nullable;

public class ProgressElement implements IElement {

    private final float progress;
    private final @Nullable Component text;
    private final IProgressStyle style;

    public ProgressElement(float progress, Component text, IProgressStyle style) {
        this.progress = Mth.clamp(progress, 0.0F, 1.0F);
        this.text = text;
        this.style = style;
    }

    public ProgressElement(FriendlyByteBuf buf) {
        this.progress = buf.readFloat();
        this.text = buf.readComponent();
        this.style = (new ProgressStyle())
                .width(buf.readInt())
                .height(buf.readInt())
                .prefix(buf.readComponent())
                .suffix(buf.readComponent())
                .borderColor(buf.readInt())
                .filledColor(buf.readInt())
                .alternateFilledColor(buf.readInt())
                .backgroundColor(buf.readInt())
                .showText(buf.readBoolean())
                .numberFormat(NumberFormat.values()[buf.readByte()])
                .lifeBar(buf.readBoolean())
                .armorBar(buf.readBoolean())
                .alignment(buf.readEnum(ElementAlignment.class));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y) {
        RenderHelper.drawThickBeveledBox(guiGraphics, x, y, x + getWidth(), y + getHeight(), 1, style.getBorderColor(),
                style.getBorderColor(), style.getBackgroundColor());
        if (progress > 0.0F) {
            var dx = (int) Math.min(progress * (getWidth() - 2), getWidth() - 2);
            if (style.getFilledColor() == style.getAlternatefilledColor()) {
                if (dx > 0) {
                    RenderHelper.drawThickBeveledBox(guiGraphics, x + 1, y + 1, x + dx + 1, y + getHeight() - 1, 1,
                            style.getFilledColor(), style.getFilledColor(), style.getFilledColor());
                }
            } else {
                for (int xx = 0; xx < x + dx; xx++) {
                    int color = (xx & 1) == 0 ? style.getFilledColor() : style.getAlternatefilledColor();
                    RenderHelper.drawVerticalLine(guiGraphics, xx, y + 1, y + getHeight() - 1, color);
                }
            }
        }
        if (style.isShowText()) {
            Minecraft mc = Minecraft.getInstance();
            Font render = mc.font;
            int textWidth = render.width(text.getVisualOrderText());
            switch (style.getAlignment()) {
                case ALIGN_BOTTOMRIGHT -> RenderHelper.renderText(mc, guiGraphics, (x + getWidth() - 3) - textWidth,
                        y + 3, text);
                case ALIGN_CENTER -> RenderHelper.renderText(mc, guiGraphics, (x + (getWidth() / 2)) - (textWidth / 2),
                        y + 3,
                        text);
                case ALIGN_TOPLEFT -> RenderHelper.renderText(mc, guiGraphics, x + 3, y + 3, text);
            }
        }
    }

    @Override
    public int getWidth() {
        float width = 0.0F;
        width += 1 * 2.0F; // Add Border width
        if (text != null) {
            Font font = Minecraft.getInstance().font;
            width += (float) (font.width(this.text) + 3);
        }
        return (int) Math.max(this.style.getWidth(), width);
    }

    @Override
    public int getHeight() {
        return this.text == null ? 8 : 14;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(progress);
        buf.writeComponent(text);
        buf.writeInt(this.style.getWidth());
        buf.writeInt(this.style.getHeight());
        buf.writeComponent(this.style.getPrefixComp());
        buf.writeComponent(this.style.getSuffixComp());
        buf.writeInt(this.style.getBorderColor());
        buf.writeInt(this.style.getFilledColor());
        buf.writeInt(this.style.getAlternatefilledColor());
        buf.writeInt(this.style.getBackgroundColor());
        buf.writeBoolean(this.style.isShowText());
        buf.writeByte(this.style.getNumberFormat().ordinal());
        buf.writeBoolean(this.style.isLifeBar());
        buf.writeBoolean(this.style.isArmorBar());
        buf.writeEnum(this.style.getAlignment());
    }

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("progress");
    }

    public static class Factory implements IElementFactory {

        @Override
        public IElement createElement(FriendlyByteBuf friendlyByteBuf) {
            return new ProgressElement(friendlyByteBuf);
        }

        @Override
        public ResourceLocation getId() {
            return GTCEu.id("progress");
        }
    }
}
