package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

public class ImageWidget extends Widget {

    protected Supplier<IGuiTexture> imageSupplier = () -> IGuiTexture.EMPTY;
    private int border;
    private int borderColor;

    public ImageWidget() {
        super(0, 0, 10, 10);
    }

    public ImageWidget(int x, int y, int width, int height, IGuiTexture image) {
        super(x, y, width, height);
        setImage(image);
    }

    public ImageWidget(int x, int y, int width, int height, Supplier<IGuiTexture> imageSupplier) {
        super(x, y, width, height);
        setImage(imageSupplier);
    }

    public ImageWidget setImage(IGuiTexture image) {
        return setImage(() -> image);
    }

    public ImageWidget setImage(Supplier<IGuiTexture> imageSupplier) {
        this.imageSupplier = imageSupplier;
        return this;
    }

    public IGuiTexture getImage() {
        return imageSupplier.get();
    }

    public ImageWidget setBorder(int border, int borderColor) {
        this.border = border;
        this.borderColor = borderColor;
        return this;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        getImage().draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }

    public int getBorder() {
        return border;
    }

    public int getBorderColor() {
        return borderColor;
    }
}
