package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class TextBoxWidget extends Widget implements IConfigurableWidget {

    public final List<String> content = new ArrayList<>();
    public int space = 10;
    public int fontSize = 9;
    public int fontColor = 0xFFFFFFFF;
    public boolean isShadow;
    public boolean isCenter;

    public TextBoxWidget() {
        super(0, 0, 80, 10);
    }

    public TextBoxWidget(int x, int y, int width, List<String> content) {
        super(x, y, width, content.size() * 10);
        this.content.addAll(content);
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
    }

    public TextBoxWidget setContent(List<String> content) {
        this.content.clear();
        this.content.addAll(content);
        return this;
    }

    public TextBoxWidget setSpace(int space) {
        this.space = space;
        return this;
    }

    public TextBoxWidget setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public TextBoxWidget setFontColor(int fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public TextBoxWidget setShadow(boolean shadow) {
        isShadow = shadow;
        return this;
    }

    public TextBoxWidget setCenter(boolean center) {
        isCenter = center;
        return this;
    }

    public int getMaxContentWidth() {
        return content.stream().mapToInt(Minecraft.getInstance().font::width).max().orElse(0);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int y = getPositionY();
        for (String line : content) {
            graphics.drawString(Minecraft.getInstance().font, line, getPositionX(), y, fontColor, isShadow);
            y += space;
        }
    }

    public boolean handleDragging(Object object) {
        return false;
    }
}
