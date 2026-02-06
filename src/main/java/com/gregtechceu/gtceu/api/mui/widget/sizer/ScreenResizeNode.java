package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

public class ScreenResizeNode extends StaticResizer {

    private final ModularScreen screen;

    public ScreenResizeNode(ModularScreen screen) {
        this.screen = screen;
    }

    public ModularScreen getScreen() {
        return screen;
    }

    @Override
    public Area getArea() {
        return screen.getScreenArea();
    }

    @Override
    public String getDebugDisplayName() {
        return "screen '" + this.screen + "'";
    }

    @Override
    public String toString() {
        return "ScreenResizeNode(" + this.screen + ")";
    }
}
