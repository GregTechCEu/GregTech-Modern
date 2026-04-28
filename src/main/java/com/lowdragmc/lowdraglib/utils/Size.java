package com.lowdragmc.lowdraglib.utils;

import java.util.Objects;

public class Size {

    public static final Size ZERO = new Size(0, 0);

    public final int width;
    public final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static Size of(int width, int height) {
        return new Size(width, height);
    }

    public static Size add(Position position) {
        return new Size(position.x, position.y);
    }

    public Size add(Size size) {
        return add(size.width, size.height);
    }

    public Size add(int width, int height) {
        return new Size(this.width + width, this.height + height);
    }

    public Size subtract(Size size) {
        return add(-size.width, -size.height);
    }

    public Size addWidth(int width) {
        return add(width, 0);
    }

    public Size addHeight(int height) {
        return add(0, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public com.lowdragmc.lowdraglib2.math.Size toLDLib2() {
        return com.lowdragmc.lowdraglib2.math.Size.of(width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Size size)) return false;
        return width == size.width && height == size.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "Size{width=%d, height=%d}".formatted(width, height);
    }
}
