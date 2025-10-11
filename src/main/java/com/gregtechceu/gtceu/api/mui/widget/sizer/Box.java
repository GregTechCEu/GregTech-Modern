package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.animation.IAnimatable;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.utils.Interpolations;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * A box with four edges.
 * Used for margins and paddings.
 */
@Accessors(fluent = true, chain = true)
public class Box implements IAnimatable<Box> {

    public static final Box SHARED = new Box();

    public static final Box ZERO = new Box();

    @Getter
    @Setter
    public int left;
    @Getter
    @Setter
    public int top;
    @Getter
    @Setter
    public int right;
    @Getter
    @Setter
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
    public Box interpolate(Box start, Box end, float t) {
        this.left = Interpolations.lerp(start.left, end.left, t);
        this.top = Interpolations.lerp(start.top, end.top, t);
        this.right = Interpolations.lerp(start.right, end.right, t);
        this.bottom = Interpolations.lerp(start.bottom, end.bottom, t);
        return this;
    }

    @Override
    public Box copyOrImmutable() {
        return new Box().set(this);
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

    public boolean isEqual(Box box) {
        return left == box.left && top == box.top && right == box.right && bottom == box.bottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isEqual((Box) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, right, bottom);
    }
}
