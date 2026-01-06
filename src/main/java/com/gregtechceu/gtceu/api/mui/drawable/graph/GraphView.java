package com.gregtechceu.gtceu.api.mui.drawable.graph;

import com.gregtechceu.gtceu.api.mui.utils.Interpolations;

public class GraphView {

    // screen rectangle
    float sx0, sy0, sx1, sy1;
    // graph rectangle
    float gx0, gy0, gx1, gy1;

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

    void setGraph(float x0, float y0, float x1, float y1) {
        this.gx0 = x0;
        this.gy0 = y0;
        this.gx1 = x1;
        this.gy1 = y1;
    }

    public float transformXGraphToScreen(float v) {
        return transform(v, gx0, gx1, sx0, sx1);
    }

    public float transformYGraphToScreen(float v) {
        // gy0 and gy1 inverted on purpose
        // screen y0 is top, graph y0 is bottom
        return transform(v, gy1, gy0, sy0, sy1);
    }

    public float transformXScreenToGraph(float v) {
        return transform(v, sx0, sx1, gx0, gx1);
    }

    public float transformYScreenToGraph(float v) {
        // gy0 and gy1 inverted on purpose
        // screen y0 is top, graph y0 is bottom
        return transform(v, sy0, sy1, gy1, gy0);
    }

    private float transform(float v, float fromMin, float fromMax, float toMin, float toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return Interpolations.lerp(toMin, toMax, v);
    }
}
