package brachy.modularui.widget.sizer;

import brachy.modularui.api.layout.ILayoutWidget;
import brachy.modularui.api.widget.IWidget;

import lombok.Getter;

import java.util.Objects;

public abstract class WidgetResizeNode extends ResizeNode {

    @Getter
    private final IWidget widget;

    protected WidgetResizeNode(IWidget widget) {
        this.widget = Objects.requireNonNull(widget);
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
