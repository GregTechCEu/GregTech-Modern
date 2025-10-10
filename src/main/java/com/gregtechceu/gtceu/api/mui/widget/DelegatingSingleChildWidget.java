package com.gregtechceu.gtceu.api.mui.widget;

public class DelegatingSingleChildWidget<W extends SingleChildWidget<W>> extends SingleChildWidget<W> {

    @Override
    public void onInit() {
        super.onInit();
        if (hasChildren()) getChild().flex().relative(getParent());
        coverChildren();
    }

    @Override
    public void postResize() {
        super.postResize();
        if (hasChildren()) {
            getArea().set(getChild().getArea());
            getArea().rx = getChild().getArea().rx;
            getArea().ry = getChild().getArea().ry;
            getChild().getArea().rx = 0;
            getChild().getArea().ry = 0;
        }
    }
}
