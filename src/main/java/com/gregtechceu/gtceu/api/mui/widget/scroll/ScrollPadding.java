package com.gregtechceu.gtceu.api.mui.widget.scroll;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.utils.Interpolations;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;

import java.util.Objects;

public class ScrollPadding extends Box {

    public static final ScrollPadding SHARED = new ScrollPadding();
    public static final ScrollPadding ZERO = new ScrollPadding();

    protected int scrollPaddingLeft;
    protected int scrollPaddingTop;
    protected int scrollPaddingRight;
    protected int scrollPaddingBottom;

    public ScrollPadding scrollPaddingAll(int all) {
        return this.scrollPaddingAll(all, all);
    }

    public ScrollPadding scrollPaddingAll(int horizontal, int vertical) {
        return this.scrollPaddingAll(horizontal, horizontal, vertical, vertical);
    }

    public ScrollPadding scrollPaddingAll(int left, int right, int top, int bottom) {
        this.scrollPaddingLeft = left;
        this.scrollPaddingTop = top;
        this.scrollPaddingRight = right;
        this.scrollPaddingBottom = bottom;
        return this;
    }

    public ScrollPadding scrollPaddingLeft(int left) {
        this.scrollPaddingLeft = left;
        return this;
    }

    public ScrollPadding scrollPaddingTop(int top) {
        this.scrollPaddingTop = top;
        return this;
    }

    public ScrollPadding scrollPaddingRight(int right) {
        this.scrollPaddingRight = right;
        return this;
    }

    public ScrollPadding scrollPaddingBottom(int bottom) {
        this.scrollPaddingBottom = bottom;
        return this;
    }

    public ScrollPadding scrollPadding(GuiAxis axis, boolean start, int val) {
        if (axis.isVertical()) {
            if (start) {
                scrollPaddingTop(val);
            } else {
                scrollPaddingBottom(val);
            }
        } else {
            if (start) {
                scrollPaddingLeft(val);
            } else {
                scrollPaddingRight(val);
            }
        }
        return this;
    }

    public ScrollPadding setScrollPadding(ScrollPadding box) {
        all(box.left, box.right, box.top, box.bottom);
        return scrollPaddingAll(box.scrollPaddingLeft, box.scrollPaddingRight, box.scrollPaddingTop,
                box.scrollPaddingBottom);
    }

    public int getLeft() {
        return this.left + this.scrollPaddingLeft;
    }

    public int getRight() {
        return this.right + this.scrollPaddingRight;
    }

    public int getTop() {
        return this.top + this.scrollPaddingTop;
    }

    public int getBottom() {
        return this.bottom + this.scrollPaddingBottom;
    }

    public int vertical() {
        return super.vertical() + this.scrollPaddingTop + this.scrollPaddingBottom;
    }

    public int horizontal() {
        return super.horizontal() + this.scrollPaddingLeft + this.scrollPaddingRight;
    }

    public int getStart(GuiAxis axis) {
        return axis.isHorizontal() ? this.left + this.scrollPaddingLeft : this.top + this.scrollPaddingTop;
    }

    public int getEnd(GuiAxis axis) {
        return axis.isHorizontal() ? this.right + this.scrollPaddingRight : this.bottom + this.scrollPaddingBottom;
    }

    public int getTotalScrollPadding(GuiAxis axis) {
        return axis.isHorizontal() ? this.scrollPaddingLeft + this.scrollPaddingRight :
                this.scrollPaddingTop + this.scrollPaddingBottom;
    }

    @Override
    public Box interpolate(Box startBox, Box endBox, float t) {
        super.interpolate(startBox, endBox, t);
        if (startBox instanceof ScrollPadding start && endBox instanceof ScrollPadding end) {
            this.scrollPaddingLeft = Interpolations.lerp(start.scrollPaddingLeft, end.scrollPaddingLeft, t);
            this.scrollPaddingTop = Interpolations.lerp(start.scrollPaddingTop, end.scrollPaddingTop, t);
            this.scrollPaddingRight = Interpolations.lerp(start.scrollPaddingRight, end.scrollPaddingRight, t);
            this.scrollPaddingBottom = Interpolations.lerp(start.scrollPaddingBottom, end.scrollPaddingBottom, t);
        }
        return this;
    }

    @Override
    public Box copyOrImmutable() {
        return new ScrollPadding().setScrollPadding(this);
    }

    @Override
    public String toString() {
        return "Box{" +
                "left=" + this.scrollPaddingLeft +
                ", top=" + this.scrollPaddingTop +
                ", right=" + this.scrollPaddingRight +
                ", bottom=" + this.scrollPaddingBottom +
                '}';
    }

    public boolean isEqual(ScrollPadding box) {
        return super.isEqual(box) && this.scrollPaddingLeft == box.scrollPaddingLeft &&
                this.scrollPaddingTop == box.scrollPaddingTop &&
                this.scrollPaddingRight == box.scrollPaddingRight &&
                this.scrollPaddingBottom == box.scrollPaddingBottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isEqual((ScrollPadding) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.top, this.right, this.bottom,
                this.scrollPaddingLeft, this.scrollPaddingTop, this.scrollPaddingRight, this.scrollPaddingBottom);
    }
}
