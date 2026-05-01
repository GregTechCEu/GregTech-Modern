package com.lowdragmc.lowdraglib.gui.texture;

import java.util.function.Supplier;

public class TextTexture extends com.lowdragmc.lowdraglib2.gui.texture.TextTexture implements IGuiTexture {

    public enum TextType {
        NORMAL,
        HIDE,
        ROLL,
        ROLL_ALWAYS,
        LEFT,
        RIGHT,
        LEFT_HIDE,
        RIGHT_HIDE,
        LEFT_ROLL,
        RIGHT_ROLL,
        LEFT_ROLL_ALWAYS
    }

    public TextType type = TextType.NORMAL;

    public TextTexture() {}

    public TextTexture(String text, int color) {
        super(text, color);
    }

    public TextTexture(String text) {
        super(text);
    }

    public TextTexture(Supplier<String> supplier) {
        super(supplier);
    }

    @Override
    public TextTexture setSupplier(Supplier<String> supplier) {
        super.setSupplier(supplier);
        return this;
    }

    @Override
    public TextTexture setBackgroundColor(int backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        return this;
    }

    @Override
    public TextTexture setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public TextTexture setDropShadow(boolean dropShadow) {
        super.setDropShadow(dropShadow);
        return this;
    }

    @Override
    public TextTexture setWidth(int width) {
        super.setWidth(width);
        return this;
    }

    public TextTexture setType(TextType type) {
        this.type = type;
        super.setType(switch (type) {
            case NORMAL -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.NORMAL;
            case HIDE -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.HIDE;
            case ROLL -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.ROLL;
            case ROLL_ALWAYS -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.ROLL_ALWAYS;
            case LEFT -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.LEFT;
            case RIGHT -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.RIGHT;
            case LEFT_HIDE -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.LEFT_HIDE;
            case LEFT_ROLL -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.LEFT_ROLL;
            case LEFT_ROLL_ALWAYS -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.LEFT_ROLL_ALWAYS;
            case RIGHT_HIDE, RIGHT_ROLL -> com.lowdragmc.lowdraglib2.gui.texture.TextTexture.TextType.RIGHT;
        });
        return this;
    }

    @Override
    public TextTexture rotate(float degree) {
        super.rotate(degree);
        return this;
    }

    @Override
    public TextTexture scale(float scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public TextTexture transform(int xOffset, int yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public TextTexture transform(float xOffset, float yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public TextTexture copy() {
        TextTexture copy = new TextTexture(text, color);
        copy.backgroundColor = backgroundColor;
        copy.width = width;
        copy.rollSpeed = rollSpeed;
        copy.dropShadow = dropShadow;
        copy.supplier = supplier;
        copy.type = type;
        return copy;
    }
}
