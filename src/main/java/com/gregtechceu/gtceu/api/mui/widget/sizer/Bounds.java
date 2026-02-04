package com.gregtechceu.gtceu.api.mui.widget.sizer;

import lombok.Getter;

@Getter
public class Bounds {

    public static final int UNLIMITED_MAX = Integer.MAX_VALUE;
    public static final int UNLIMITED_MIN = Integer.MIN_VALUE;

    private int minWidth = UNLIMITED_MIN, minHeight = UNLIMITED_MIN;
    private int maxWidth = UNLIMITED_MAX, maxHeight = UNLIMITED_MAX;

    public Bounds set(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        return this;
    }

    public Bounds max(int width, int height) {
        this.maxWidth = width;
        this.maxHeight = height;
        return this;
    }

    public Bounds min(int width, int height) {
        this.minWidth = width;
        this.minHeight = height;
        return this;
    }

    public Bounds exact(int w, int h) {
        return set(w, h, w, h);
    }
}
