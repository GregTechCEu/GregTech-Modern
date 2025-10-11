package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.ITextLine;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;

import lombok.Getter;

public class Spacer implements ITextLine {

    public static final Spacer SPACER_2PX = new Spacer(2);
    public static final Spacer LINE_SPACER = new Spacer(FontRenderHelper.getDefaultTextHeight());

    public static Spacer of(int space) {
        if (space == 2) return SPACER_2PX;
        if (space == LINE_SPACER.space) return LINE_SPACER;
        return new Spacer(space);
    }

    @Getter
    private final int space;

    protected Spacer(int space) {
        this.space = space;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight(Font font) {
        return this.space;
    }

    @Override
    public void draw(GuiContext context, Font font, float x, float y, int color, boolean shadow, int availableWidth,
                     int availableHeight) {}

    @Override
    public Object getHoveringElement(Font font, int x, int y) {
        return null;
    }
}
