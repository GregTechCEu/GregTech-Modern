package com.lowdragmc.lowdraglib.gui.texture;

import java.awt.Color;

public class ColorRectTexture extends com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture implements IGuiTexture {

    public float radiusLT;
    public float radiusLB;
    public float radiusRT;
    public float radiusRB;

    public ColorRectTexture() {}

    public ColorRectTexture(int color) {
        super(color);
    }

    public ColorRectTexture(Color color) {
        super(color);
    }

    public ColorRectTexture setRadius(float radius) {
        return setRadiusLT(radius).setRadiusLB(radius).setRadiusRT(radius).setRadiusRB(radius);
    }

    public ColorRectTexture setLeftRadius(float radius) {
        return setRadiusLT(radius).setRadiusLB(radius);
    }

    public ColorRectTexture setRightRadius(float radius) {
        return setRadiusRT(radius).setRadiusRB(radius);
    }

    public ColorRectTexture setTopRadius(float radius) {
        return setRadiusLT(radius).setRadiusRT(radius);
    }

    public ColorRectTexture setBottomRadius(float radius) {
        return setRadiusLB(radius).setRadiusRB(radius);
    }

    @Override
    public ColorRectTexture setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public ColorRectTexture rotate(float degree) {
        super.rotate(degree);
        return this;
    }

    @Override
    public ColorRectTexture scale(float scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public ColorRectTexture transform(int xOffset, int yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public ColorRectTexture transform(float xOffset, float yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    public ColorRectTexture setRadiusLT(float radiusLT) {
        this.radiusLT = radiusLT;
        return this;
    }

    public ColorRectTexture setRadiusLB(float radiusLB) {
        this.radiusLB = radiusLB;
        return this;
    }

    public ColorRectTexture setRadiusRT(float radiusRT) {
        this.radiusRT = radiusRT;
        return this;
    }

    public ColorRectTexture setRadiusRB(float radiusRB) {
        this.radiusRB = radiusRB;
        return this;
    }

    @Override
    public ColorRectTexture copy() {
        return new ColorRectTexture(color).setRadiusLT(radiusLT).setRadiusLB(radiusLB)
                .setRadiusRT(radiusRT).setRadiusRB(radiusRB);
    }
}
