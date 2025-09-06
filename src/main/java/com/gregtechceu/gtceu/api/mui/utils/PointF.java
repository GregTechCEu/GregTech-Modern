package com.gregtechceu.gtceu.api.mui.utils;

import lombok.Getter;
import lombok.Setter;

public final class PointF {

    public static final PointF ZERO = new PointF(0, 0);

    @Getter
    @Setter
    public float x;
    @Getter
    @Setter
    public float y;

    public PointF() {
        this(0, 0);
    }

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF(PointF point) {
        this(point.x, point.y);
    }

    public PointF(Point point) {
        this(point.x, point.y);
    }

    public PointF copy() {
        return new PointF(this);
    }

    public PointF inverse() {
        return new PointF(-this.x, -this.y);
    }

    public PointF move(float x, float y) {
        return new PointF(this.x + x, this.y + y);
    }

    public PointF move(PointF point) {
        return move(point.x, point.y);
    }

    public PointF offset(float x, float y) {
        return move(x, y);
    }

    public PointF offset(PointF point) {
        return move(point);
    }

    public PointF set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public PointF set(PointF point) {
        return set(point.x, point.y);
    }

    public PointF set(Point point) {
        return set(point.x, point.y);
    }
}
