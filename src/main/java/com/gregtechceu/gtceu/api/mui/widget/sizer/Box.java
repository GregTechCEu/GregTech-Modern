package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.animation.IAnimatable;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.utils.Interpolations;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.gson.JsonObject;
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

    public static final Box ONE = new Box().all(1);

    @Getter
    @Setter
    protected int left;
    @Getter
    @Setter
    protected int top;
    @Getter
    @Setter
    protected int right;
    @Getter
    @Setter
    protected int bottom;

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

    public Box set(GuiAxis axis, boolean start, int val) {
        if (axis.isVertical()) {
            if (start) {
                top(val);
            } else {
                bottom(val);
            }
        } else {
            if (start) {
                left(val);
            } else {
                right(val);
            }
        }
        return this;
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

    public void fromJson(JsonObject json) {
        all(JsonHelper.getInt(json, 0, "margin"));
        if (json.has("marginHorizontal")) {
            this.left = json.get("marginHorizontal").getAsInt();
            this.right = this.left;
        }
        if (json.has("marginVertical")) {
            this.top = json.get("marginVertical").getAsInt();
            this.bottom = this.top;
        }
        this.top = JsonHelper.getInt(json, this.top, "marginTop");
        this.bottom = JsonHelper.getInt(json, this.bottom, "marginBottom");
        this.left = JsonHelper.getInt(json, this.left, "marginLeft");
        this.right = JsonHelper.getInt(json, this.right, "marginRight");
    }

    public void toJson(JsonObject json) {
        json.addProperty("marginTop", this.top);
        json.addProperty("marginBottom", this.bottom);
        json.addProperty("marginLeft", this.left);
        json.addProperty("marginRight", this.right);
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
