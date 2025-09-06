package com.gregtechceu.gtceu.api.mui.utils;

import lombok.Getter;
import lombok.Setter;

public final class Point {

    public static final Point ZERO = new Point(0, 0);

    @Getter
    @Setter
    public int x;
    @Getter
    @Setter
    public int y;

    public Point() {
        this(0, 0);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this(point.x, point.y);
    }

    public Point copy() {
        return new Point(this);
    }

    public Point inverse() {
        return new Point(-this.x, -this.y);
    }

    public Point move(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point move(Point point) {
        return this.move(point.x, point.y);
    }

    public Point offset(int x, int y) {
        return move(x, y);
    }

    public Point offset(Point point) {
        return this.move(point);
    }

    public Point set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Point set(Point point) {
        return set(point.x, point.y);
    }

    public Point set(PointF point) {
        return set(Math.round(point.x), Math.round(point.y));
    }
}
