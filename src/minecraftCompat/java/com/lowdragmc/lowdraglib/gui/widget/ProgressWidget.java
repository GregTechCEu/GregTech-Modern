package com.lowdragmc.lowdraglib.gui.widget;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import java.util.function.DoubleSupplier;
import java.util.function.Function;

public class ProgressWidget extends Widget {

    public static final DoubleSupplier JEIProgress = () -> 1.0;
    public DoubleSupplier progressSupplier = () -> 0.0;
    protected ProgressTexture progressTexture = new ProgressTexture();
    protected IGuiTexture overlayTexture = IGuiTexture.EMPTY;
    protected double lastProgressValue;

    public ProgressWidget() {
        super(0, 0, 10, 10);
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height,
                          ResourceTexture texture) {
        this(progressSupplier, x, y, width, height, new ProgressTexture(IGuiTexture.EMPTY, texture));
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height,
                          ProgressTexture progressTexture) {
        super(x, y, width, height);
        this.progressSupplier = progressSupplier;
        this.progressTexture = progressTexture;
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        this(progressSupplier, x, y, width, height, new ProgressTexture());
    }

    public ProgressWidget setProgressTexture(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        progressTexture.setTexture(emptyBarArea, filledBarArea);
        return this;
    }

    public ProgressWidget setFillDirection(ProgressTexture.FillDirection fillDirection) {
        progressTexture.setFillDirection(fillDirection);
        return this;
    }

    @Override
    public void updateScreen() {
        lastProgressValue = progressSupplier.getAsDouble();
        progressTexture.setProgress(lastProgressValue);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        updateScreen();
        progressTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        overlayTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }

    public ProgressWidget setProgressSupplier(DoubleSupplier progressSupplier) {
        this.progressSupplier = progressSupplier;
        return this;
    }

    public ProgressWidget setDynamicHoverTips(Function<Double, String> dynamicHoverTips) {
        return this;
    }

    public ProgressWidget setProgressTexture(IGuiTexture progressTexture) {
        this.progressTexture.setTexture(IGuiTexture.EMPTY, progressTexture);
        return this;
    }

    public ProgressWidget setOverlayTexture(IGuiTexture overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public double getLastProgressValue() {
        return lastProgressValue;
    }
}
