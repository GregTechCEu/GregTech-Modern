package brachy.modularui.widget.sizer;

import brachy.modularui.screen.ModularScreen;

import lombok.Getter;

public class ScreenResizeNode extends StaticResizer {

    @Getter
    private final ModularScreen screen;

    public ScreenResizeNode(ModularScreen screen) {
        this.screen = screen;
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
    public ScreenResizeNode copy() {
        return new ScreenResizeNode(this.screen);
    }

    @Override
    public String toString() {
        return "ScreenResizeNode(" + this.screen + ")";
    }
}
