package com.gregtechceu.gtceu.api.mui.utils;

import net.minecraft.client.renderer.Rect2i;

import lombok.Getter;
import lombok.Setter;

public class Rectangle {

    @Getter
    @Setter
    public int x;
    @Getter
    @Setter
    public int y;
    @Getter
    @Setter
    public int width;
    @Getter
    @Setter
    public int height;

    public Rectangle() {
        this(0, 0, 0, 0);
    }

    public Rectangle(Rectangle toCopy) {
        this(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle intersect(Rectangle other) {
        int i = this.x;
        int j = this.y;
        int k = this.x + this.width;
        int l = this.y + this.height;
        int i1 = other.getX();
        int j1 = other.getY();
        int k1 = i1 + other.getWidth();
        int l1 = j1 + other.getHeight();
        this.x = Math.max(i, i1);
        this.y = Math.max(j, j1);
        this.width = Math.max(0, Math.min(k, k1) - this.x);
        this.height = Math.max(0, Math.min(l, l1) - this.y);
        return this;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setBounds(Rectangle toCopy) {
        setBounds(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public PointF getCenter() {
        return new PointF(x + width / 2.0f, y + height / 2.0f);
    }

    public Rect2i asRect2i() {
        return new Rect2i(this.x, this.y, this.width, this.height);
    }

    public boolean contains(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    public boolean contains(Point point) {
        return contains(point.x, point.y);
    }
}
