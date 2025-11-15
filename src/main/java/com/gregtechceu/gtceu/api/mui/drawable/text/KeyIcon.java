package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;

import lombok.Getter;

/**
 * An icon which represents a {@link IKey} object.
 * Note: This class assumes the string will be a single line!
 */
public class KeyIcon implements IIcon {

    @Getter
    private final IKey key;
    private Font overrideFont;
    private final Box margin = new Box();
    private boolean expandWidth, expandHeight;

    public KeyIcon(IKey key) {
        this.key = key;
    }

    public Font getFont() {
        return this.overrideFont != null ? this.overrideFont : MCHelper.getFont();
    }

    @Override
    public int getWidth() {
        return expandWidth ? 0 : getActualWidth();
    }

    @Override
    public int getHeight() {
        return expandHeight ? 0 : getActualHeight();
    }

    @Override
    public int getDefaultWidth() {
        return this.key.getDefaultWidth();
    }

    @Override
    public int getDefaultHeight() {
        return this.key.getDefaultHeight();
    }

    public int getActualWidth() {
        return getFont().width(key.get()) + this.margin.horizontal();
    }

    public int getActualHeight() {
        return getFont().lineHeight + this.margin.vertical();
    }

    @Override
    public Box getMargin() {
        return null;
    }

    @Override
    public IKey getWrappedDrawable() {
        return key;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        int w = getActualWidth(), h = getActualHeight();
        x += (int) (width / 2f - w / 2f);
        y += (int) (height / 2f - h / 2f);
        this.key.draw(context, x, y, width, height, widgetTheme);
    }

    public KeyIcon expandWidth() {
        this.expandWidth = true;
        return this;
    }

    public KeyIcon expandHeight() {
        this.expandHeight = true;
        return this;
    }

    public KeyIcon margin(int left, int right, int top, int bottom) {
        this.margin.all(left, right, top, bottom);
        return this;
    }

    public KeyIcon margin(int horizontal, int vertical) {
        this.margin.all(horizontal, vertical);
        return this;
    }

    public KeyIcon margin(int all) {
        this.margin.all(all);
        return this;
    }

    public KeyIcon marginLeft(int val) {
        this.margin.left(val);
        return this;
    }

    public KeyIcon marginRight(int val) {
        this.margin.right(val);
        return this;
    }

    public KeyIcon marginTop(int val) {
        this.margin.top(val);
        return this;
    }

    public KeyIcon marginBottom(int val) {
        this.margin.bottom(val);
        return this;
    }

    public KeyIcon font(Font fr) {
        this.overrideFont = fr;
        return this;
    }

    @Override
    public String toString() {
        return "KeyIcon(" + this.key.get() + ")";
    }
}
