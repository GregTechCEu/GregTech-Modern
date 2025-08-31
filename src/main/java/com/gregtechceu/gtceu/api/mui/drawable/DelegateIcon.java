package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

public class DelegateIcon implements IIcon {

    private IIcon icon;

    public DelegateIcon(IIcon icon) {
        this.icon = icon;
    }

    @Override
    public int getWidth() {
        return this.icon.getWidth();
    }

    @Override
    public int getHeight() {
        return this.icon.getHeight();
    }

    @Override
    public Box getMargin() {
        return this.icon.getMargin();
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        this.icon.draw(context, x, y, width, height, widgetTheme);
    }

    public IIcon getDelegate() {
        return icon;
    }

    public IIcon findRootDelegate() {
        IIcon icon = this;
        while (icon instanceof DelegateIcon di) {
            icon = di.getDelegate();
        }
        return icon;
    }

    protected void setDelegate(IIcon icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.icon + ")";
    }
}
