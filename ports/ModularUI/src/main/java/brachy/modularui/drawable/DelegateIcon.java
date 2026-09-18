package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.sizer.Box;

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

    @Override
    public IIcon getWrappedDrawable() {
        return icon;
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
    public boolean equals(Object o) {
        if (!(o instanceof DelegateIcon that)) return false;

        return icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return icon.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.icon + ")";
    }
}
