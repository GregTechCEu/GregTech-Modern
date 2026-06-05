package com.gregtechceu.gtceu.common.mui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import org.jetbrains.annotations.Nullable;

public record CycleDrawable(IDrawable... drawables) implements IDrawable {

    public @Nullable IDrawable getCurrent() {
        if (this.drawables.length == 0) return null;
        return this.drawables[Math.abs((int) (System.currentTimeMillis() / 1000) % this.drawables.length)];
    }

    @Override
    public boolean canApplyTheme() {
        IDrawable current = getCurrent();
        return current != null && current.canApplyTheme();
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        IDrawable current = getCurrent();
        if (current == null) return;

        current.draw(context, x, y, width, height, widgetTheme);
    }
}
