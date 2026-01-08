package com.gregtechceu.gtceu.api.mui.drawable.graph;

import lombok.Getter;
import lombok.Setter;

public class GraphView {

    @Setter
    @Getter
    float aspectRatio = 0;
    // screen rectangle
    float sx0, sy0, sx1, sy1;
    // graph rectangle
    double gx0, gy0, gx1, gy1;

    float zeroX, zeroY;

    void postResize() {
        if (this.aspectRatio > 0) {
            float w = sx1 - sx0, h = sy1 - sy0;
            float properW = this.aspectRatio * h;
            if (w > properW) {
                float d = w - properW;
                sx0 += d / 2;
                sx1 -= d / 2;
            } else if (properW > w) {
                float properH = w / this.aspectRatio;
                float d = h - properH;
                sy0 += d / 2;
                sy1 -= d / 2;
            }
        }
        this.zeroX = g2sX(0);
        this.zeroY = g2sY(0);
    }

    boolean setScreen(float x0, float y0, float x1, float y1) {
        if (x0 != this.sx0 || y0 != this.sy0 || x1 != this.sx1 || y1 != this.sy1) {
            this.sx0 = x0;
            this.sy0 = y0;
            this.sx1 = x1;
            this.sy1 = y1;
            return true;
        }
        return false;
    }

    void setGraph(double x0, double y0, double x1, double y1) {
        this.gx0 = x0;
        this.gy0 = y0;
        this.gx1 = x1;
        this.gy1 = y1;
        this.zeroX = g2sX(0);
        this.zeroY = g2sY(0);
    }

    public float g2sX(double v) {
        return (float) transform(v, gx0, gx1, sx0, sx1);
    }

    public float g2sY(double v) {
        // gy0 and gy1 inverted on purpose
        // screen y0 is top, graph y0 is bottom
        return (float) transform(v, gy1, gy0, sy0, sy1);
    }

    public double g2sScaleX() {
        return scale(gx0, gx1, sx0, sx1);
    }

    public double g2sScaleY() {
        return scale(gy1, gy0, sy0, sy1);
    }

    public double s2gX(float v) {
        return transform(v, sx0, sx1, gx0, gx1);
    }

    public double s2gY(float v) {
        // gy0 and gy1 inverted on purpose
        // screen y0 is top, graph y0 is bottom
        return transform(v, sy0, sy1, gy1, gy0);
    }

    private double transform(double v, double fromMin, double fromMax, double toMin, double toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return toMin + (toMax - toMin) * v;
    }

    private double scale(double fromMin, double fromMax, double toMin, double toMax) {
        return (toMax - toMin) / (fromMax - fromMin);
    }

    public double getGraphX0() {
        return gx0;
    }

    public double getGraphX1() {
        return gx1;
    }

    public double getGraphY0() {
        return gy0;
    }

    public double getGraphY1() {
        return gy1;
    }

    public float getScreenX0() {
        return sx0;
    }

    public float getScreenX1() {
        return sx1;
    }

    public float getScreenY0() {
        return sy0;
    }

    public float getScreenY1() {
        return sy1;
    }

    public float getScreenWidth() {
        return sx1 - sx0;
    }

    public float getScreenHeight() {
        return sy1 - sy0;
    }

    public double getGraphWidth() {
        return gx1 - gx0;
    }

    public double getGraphHeight() {
        return gy1 - gy0;
    }
}
