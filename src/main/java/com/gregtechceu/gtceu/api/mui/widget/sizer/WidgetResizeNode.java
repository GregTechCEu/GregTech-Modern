package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

import java.util.Objects;

public abstract class WidgetResizeNode extends ResizeNode {

    private final IWidget widget;

    protected WidgetResizeNode(IWidget widget) {
        this.widget = Objects.requireNonNull(widget);
    }

    public IWidget getWidget() {
        return widget;
    }

    @Override
    public Area getArea() {
        return widget.getArea();
    }

    @Override
    public void initResizing(boolean onOpen) {
        super.initResizing(onOpen);
        this.widget.beforeResize(onOpen);
    }

    @Override
    public void onResized() {
        super.onResized();
        this.widget.onResized();
    }

    @Override
    public void postFullResize() {
        super.postFullResize();
        this.widget.postResize();
    }

    @Override
    public boolean isLayout() {
        return this.widget instanceof ILayoutWidget;
    }

    @Override
    public boolean layoutChildren() {
        if (this.widget instanceof ILayoutWidget layoutWidget) {
            return layoutWidget.layoutWidgets();
        }
        return true;
    }

    @Override
    public boolean postLayoutChildren() {
        if (this.widget instanceof ILayoutWidget layoutWidget) {
            return layoutWidget.postLayoutWidgets();
        }
        return true;
    }

    @Override
    public String getDebugDisplayName() {
        return "widget '" + this.widget + "' of screen '" + this.widget.getScreen() + "'";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.widget + ")";
    }
}
