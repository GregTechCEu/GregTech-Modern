package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;

public abstract class StaticResizer extends ResizeNode {

    private boolean childrenCalculated = false;

    public StaticResizer() {}

    @Override
    public void initResizing(boolean onOpen) {
        super.initResizing(onOpen);
        setChildrenResized(false);
    }

    @Override
    public boolean isXCalculated() {
        return true;
    }

    @Override
    public boolean isYCalculated() {
        return true;
    }

    @Override
    public boolean isWidthCalculated() {
        return true;
    }

    @Override
    public boolean isHeightCalculated() {
        return true;
    }

    @Override
    public boolean areChildrenCalculated() {
        return this.childrenCalculated;
    }

    @Override
    public boolean isLayoutDone() {
        return true;
    }

    @Override
    public boolean canRelayout(boolean isParentLayout) {
        return false;
    }

    @Override
    public boolean isXMarginPaddingApplied() {
        return true;
    }

    @Override
    public boolean isYMarginPaddingApplied() {
        return true;
    }

    @Override
    public boolean resize(boolean isParentLayout) {
        return true;
    }

    @Override
    public boolean postResize() {
        return true;
    }

    @Override
    public void setChildrenResized(boolean resized) {
        this.childrenCalculated = resized;
    }

    @Override
    public void setLayoutDone(boolean done) {}

    @Override
    public void setResized(boolean x, boolean y, boolean w, boolean h) {}

    @Override
    public void setXMarginPaddingApplied(boolean b) {}

    @Override
    public void setYMarginPaddingApplied(boolean b) {}

    @Override
    public boolean hasYPos() {
        return true;
    }

    @Override
    public boolean hasXPos() {
        return true;
    }

    @Override
    public boolean hasHeight() {
        return true;
    }

    @Override
    public boolean hasWidth() {
        return true;
    }

    @Override
    public boolean hasStartPos(GuiAxis axis) {
        return true;
    }

    @Override
    public boolean hasEndPos(GuiAxis axis) {
        return false;
    }

    @Override
    public boolean hasFixedSize() {
        return true;
    }

    @Override
    public boolean isFullSize() {
        return false;
    }
}
