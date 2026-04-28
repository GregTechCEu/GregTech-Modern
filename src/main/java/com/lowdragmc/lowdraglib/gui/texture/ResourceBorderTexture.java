package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.resources.ResourceLocation;

public class ResourceBorderTexture extends ResourceTexture {

    public static final ResourceBorderTexture BORDERED_BACKGROUND = new ResourceBorderTexture(
            "lowdraglib:textures/gui/bordered_background.png", 4, 4, 16, 16);
    public static final ResourceBorderTexture BORDERED_BACKGROUND_INVERSE = new ResourceBorderTexture(
            "lowdraglib:textures/gui/bordered_background_inverse.png", 4, 4, 16, 16);
    public static final ResourceBorderTexture BORDERED_BACKGROUND_BLUE = new ResourceBorderTexture(
            "lowdraglib:textures/gui/bordered_background_blue.png", 4, 4, 16, 16);
    public static final ResourceBorderTexture BUTTON_COMMON = new ResourceBorderTexture(
            "lowdraglib:textures/gui/button_common.png", 2, 2, 20, 20);
    public static final ResourceBorderTexture BAR = new ResourceBorderTexture("lowdraglib:textures/gui/bar.png", 2, 2,
            20, 20);
    public static final ResourceBorderTexture SELECTED = new ResourceBorderTexture(
            "lowdraglib:textures/gui/selected.png", 2, 2, 20, 20);

    public Size borderSize = Size.ZERO;
    public Size imageSize = Size.ZERO;

    public ResourceBorderTexture() {}

    public ResourceBorderTexture(String imageLocation, int borderWidth, int borderHeight, int imageWidth,
                                 int imageHeight) {
        super(ResourceLocation.parse(imageLocation));
        setBorderSize(borderWidth, borderHeight);
        setImageSize(imageWidth, imageHeight);
        setBorder(borderWidth, borderHeight, borderWidth, borderHeight);
    }

    public ResourceBorderTexture setBorderSize(int width, int height) {
        borderSize = new Size(width, height);
        return this;
    }

    public ResourceBorderTexture setImageSize(int width, int height) {
        imageSize = new Size(width, height);
        return this;
    }

    @Override
    public ResourceBorderTexture setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public ResourceBorderTexture copy() {
        ResourceBorderTexture copy = new ResourceBorderTexture();
        copy.imageLocation = imageLocation;
        copy.offsetX = offsetX;
        copy.offsetY = offsetY;
        copy.imageWidth = imageWidth;
        copy.imageHeight = imageHeight;
        copy.borderSize = borderSize;
        copy.imageSize = imageSize;
        copy.setImageLocation(imageLocation.toIdentifier());
        copy.setBorder(borderSize.width, borderSize.height, borderSize.width, borderSize.height);
        copy.setColor(color);
        return copy;
    }
}
