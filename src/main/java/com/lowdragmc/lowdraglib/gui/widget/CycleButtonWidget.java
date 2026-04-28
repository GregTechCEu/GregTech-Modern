package com.lowdragmc.lowdraglib.gui.widget;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class CycleButtonWidget extends Widget {

    private final int count;
    private int index;
    private Int2ObjectFunction<IGuiTexture> textureGetter;
    private IntConsumer onChanged = ignored -> {};
    private IntSupplier indexSupplier;

    public CycleButtonWidget(int x, int y, int width, int height, int count,
                             Int2ObjectFunction<IGuiTexture> textureGetter, IntConsumer onChanged) {
        super(x, y, width, height);
        this.count = count;
        this.textureGetter = textureGetter;
        this.onChanged = onChanged;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int current = indexSupplier == null ? index : indexSupplier.getAsInt();
        textureGetter.get(current).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(),
                getSizeHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) return false;
        index = (index + 1) % Math.max(1, count);
        onChanged.accept(index);
        return true;
    }

    public CycleButtonWidget setTexture(Int2ObjectFunction<IGuiTexture> textureGetter) {
        this.textureGetter = textureGetter;
        return this;
    }

    public CycleButtonWidget setOnChanged(IntConsumer onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public CycleButtonWidget setIndexSupplier(IntSupplier indexSupplier) {
        this.indexSupplier = indexSupplier;
        return this;
    }
}
