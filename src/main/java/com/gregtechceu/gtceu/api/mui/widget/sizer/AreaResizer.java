package com.gregtechceu.gtceu.api.mui.widget.sizer;

public class AreaResizer extends StaticResizer {

    private final Area area;

    public AreaResizer(Area area) {
        this.area = area;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public String getDebugDisplayName() {
        return "";
    }

    @Override
    public String toString() {
        return "AreaResizer(" + this.area + ")";
    }
}
