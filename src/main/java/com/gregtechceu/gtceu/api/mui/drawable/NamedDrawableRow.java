package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class NamedDrawableRow implements IDrawable {

    @Getter
    private IKey name;
    @Getter
    private IIcon drawable;

    public NamedDrawableRow() {
        this(null, null);
    }

    public NamedDrawableRow(@Nullable IKey name, @Nullable IIcon drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.name != null) {
            this.name.drawAligned(context, x, y, width, height, widgetTheme, Alignment.CenterLeft);
        }
        if (this.drawable != null) {
            int wd = this.drawable.getWidth() + this.drawable.getMargin().horizontal();
            int xd = x + width - wd;
            this.drawable.draw(context, xd, y, wd, height, widgetTheme);
        }
    }

    @Override
    public int getDefaultWidth() {
        int w = 0;
        if (this.name != null) w += this.name.getDefaultWidth();
        if (this.drawable != null) w += this.drawable.getWidth();
        return w;
    }

    @Override
    public int getDefaultHeight() {
        int h = 0;
        if (this.name != null) h += this.name.getDefaultHeight();
        if (this.drawable != null) h += this.drawable.getHeight();
        return h;
    }

    public NamedDrawableRow name(@Nullable IKey key) {
        this.name = key;
        return this;
    }

    public NamedDrawableRow drawable(@Nullable IIcon drawable) {
        this.drawable = drawable;
        return this;
    }
}
