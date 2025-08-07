package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;

/**
 * A box with four edges.
 * Used for margins and paddings.
 */
public class Box {

    public static final Box SHARED = new Box();

    public static final Box ZERO = new Box();

    public int left;
    public int top;
    public int right;
    public int bottom;

    public Box all(int all) {
        return this.all(all, all);
    }

    public Box all(int horizontal, int vertical) {
        return this.all(horizontal, horizontal, vertical, vertical);
    }

    public Box all(int left, int right, int top, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        return this;
    }

    public Box left(int left) {
        this.left = left;
        return this;
    }

    public Box top(int top) {
        this.top = top;
        return this;
    }

    public Box right(int right) {
        this.right = right;
        return this;
    }

    public Box bottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public Box set(Box box) {
        return all(box.left, box.right, box.top, box.bottom);
    }

    public int vertical() {
        return this.top + this.bottom;
    }

    public int horizontal() {
        return this.left + this.right;
    }

    public int getTotal(GuiAxis axis) {
        return axis.isHorizontal() ? horizontal() : vertical();
    }

    public int getStart(GuiAxis axis) {
        return axis.isHorizontal() ? this.left : this.top;
    }

    public int getEnd(GuiAxis axis) {
        return axis.isHorizontal() ? this.right : this.bottom;
    }

    @Override
    public String toString() {
        return "Box{" +
                "left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", bottom=" + bottom +
                '}';
    }
}
