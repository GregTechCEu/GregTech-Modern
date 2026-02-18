package com.gregtechceu.gtceu.api.mui.utils;

import lombok.Getter;
import lombok.Setter;

public class RectangleF {

    @Getter
    @Setter
    public float x;
    @Getter
    @Setter
    public float y;
    @Getter
    @Setter
    public float width;
    @Getter
    @Setter
    public float height;

    public RectangleF() {
        this(0, 0, 0, 0);
    }

    public RectangleF(RectangleF toCopy) {
        this(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    public RectangleF(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public RectangleF intersect(RectangleF other) {
        float i = this.x;
        float j = this.y;
        float k = this.x + this.width;
        float l = this.y + this.height;
        float i1 = other.getX();
        float j1 = other.getY();
        float k1 = i1 + other.getWidth();
        float l1 = j1 + other.getHeight();
        this.x = Math.max(i, i1);
        this.y = Math.max(j, j1);
        this.width = Math.max(0, Math.min(k, k1) - this.x);
        this.height = Math.max(0, Math.min(l, l1) - this.y);
        return this;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setBounds(RectangleF toCopy) {
        setBounds(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public PointF getCenter() {
        return new PointF(x + width / 2.0f, y + height / 2.0f);
    }

    public boolean contains(float x, float y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    public boolean contains(Point point) {
        return contains(point.x, point.y);
    }

    public float u0() {
        return x;
    }

    public float v0() {
        return y;
    }

    public float u1() {
        return width;
    }

    public float v1() {
        return height;
    }
}
