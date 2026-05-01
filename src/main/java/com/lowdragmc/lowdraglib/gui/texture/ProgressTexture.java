package com.lowdragmc.lowdraglib.gui.texture;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;

public class ProgressTexture extends TransformTexture {

    public enum FillDirection {

        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        UP_TO_DOWN,
        DOWN_TO_UP,
        ALWAYS_FULL;

        public double getDrawnU(double progress) {
            return this == RIGHT_TO_LEFT ? 1 - clamp(progress) : 0;
        }

        public double getDrawnV(double progress) {
            return this == DOWN_TO_UP ? 1 - clamp(progress) : 0;
        }

        public double getDrawnWidth(double progress) {
            return switch (this) {
                case UP_TO_DOWN, DOWN_TO_UP -> 1;
                case ALWAYS_FULL -> 1;
                default -> clamp(progress);
            };
        }

        public double getDrawnHeight(double progress) {
            return switch (this) {
                case LEFT_TO_RIGHT, RIGHT_TO_LEFT -> 1;
                case ALWAYS_FULL -> 1;
                default -> clamp(progress);
            };
        }

        private static double clamp(double progress) {
            return Math.max(0, Math.min(1, progress));
        }
    }

    private IGuiTexture emptyBarArea = IGuiTexture.EMPTY;
    private IGuiTexture filledBarArea = IGuiTexture.EMPTY;
    private FillDirection fillDirection = FillDirection.LEFT_TO_RIGHT;
    private double progress;

    public ProgressTexture() {}

    public ProgressTexture(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        setTexture(emptyBarArea, filledBarArea);
    }

    public ProgressTexture setTexture(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = filledBarArea;
        return this;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public ProgressTexture setFillDirection(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
        return this;
    }

    public FillDirection getFillDirection() {
        return fillDirection;
    }

    public IGuiTexture getEmptyBarArea() {
        return emptyBarArea;
    }

    public IGuiTexture getFilledBarArea() {
        return filledBarArea;
    }

    public double getProgress() {
        return progress;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        var context = GUIContext.of(graphics, mouseX, mouseY, 0);
        context.drawTexture(emptyBarArea, x, y, width, height);
        float amount = (float) Math.max(0, Math.min(1, progress));
        if (fillDirection == FillDirection.ALWAYS_FULL) amount = 1;
        switch (fillDirection) {
            case LEFT_TO_RIGHT, ALWAYS_FULL -> context.drawTexture(filledBarArea, x, y, width * amount, height);
            case RIGHT_TO_LEFT -> context.drawTexture(filledBarArea, x + width * (1 - amount), y, width * amount,
                    height);
            case UP_TO_DOWN -> context.drawTexture(filledBarArea, x, y, width, height * amount);
            case DOWN_TO_UP -> context.drawTexture(filledBarArea, x, y + height * (1 - amount), width, height * amount);
        }
    }
}
