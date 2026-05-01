package com.lowdragmc.lowdraglib.gui.texture;

public abstract class TransformTexture extends com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture
                                       implements IGuiTexture {

    @Override
    public TransformTexture rotate(float degree) {
        super.rotate(degree);
        return this;
    }

    @Override
    public TransformTexture setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public TransformTexture scale(float scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public TransformTexture transform(int xOffset, int yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    public TransformTexture transform(float xOffset, float yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public TransformTexture copy() {
        return this;
    }
}
