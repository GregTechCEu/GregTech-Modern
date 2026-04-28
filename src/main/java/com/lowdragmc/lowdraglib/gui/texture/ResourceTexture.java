package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;

public class ResourceTexture extends TransformTexture {

    public ResourceLocation imageLocation;
    public float offsetX;
    public float offsetY;
    public float imageWidth = 1;
    public float imageHeight = 1;

    public ResourceTexture() {}

    public ResourceTexture(ResourceLocation imageLocation, float offsetX, float offsetY, float imageWidth,
                           float imageHeight) {
        this.imageLocation = imageLocation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        setImageLocation(imageLocation.toIdentifier());
    }

    public ResourceTexture(String imageLocation) {
        this(ResourceLocation.parse(imageLocation));
    }

    public ResourceTexture(ResourceLocation imageLocation) {
        this(imageLocation, 0, 0, 1, 1);
    }

    public ResourceTexture(Identifier imageLocation) {
        this(ResourceLocation.fromIdentifier(imageLocation));
    }

    public ResourceTexture getSubTexture(float offsetX, float offsetY, float imageWidth, float imageHeight) {
        return new ResourceTexture(imageLocation, this.offsetX + offsetX * this.imageWidth,
                this.offsetY + offsetY * this.imageHeight, this.imageWidth * imageWidth, this.imageHeight * imageHeight)
                .setColor(color);
    }

    public ResourceTexture getSubTexture(double offsetX, double offsetY, double imageWidth, double imageHeight) {
        return getSubTexture((float) offsetX, (float) offsetY, (float) imageWidth, (float) imageHeight);
    }

    @Override
    public ResourceTexture setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public ResourceTexture setImageLocation(Identifier imageLocation) {
        super.setImageLocation(imageLocation);
        this.imageLocation = ResourceLocation.fromIdentifier(imageLocation);
        return this;
    }

    @Override
    public ResourceTexture setBorder(int left, int top, int right, int bottom) {
        super.setBorder(left, top, right, bottom);
        return this;
    }

    @Override
    public ResourceTexture rotate(float degree) {
        super.rotate(degree);
        return this;
    }

    @Override
    public ResourceTexture scale(float scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public ResourceTexture transform(int xOffset, int yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public ResourceTexture transform(float xOffset, float yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public ResourceTexture copy() {
        return new ResourceTexture(imageLocation, offsetX, offsetY, imageWidth, imageHeight).setColor(color);
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        GUIContext.of(graphics, mouseX, mouseY, 0).drawTexture(this, x, y, width, height);
    }

    public static ResourceTexture fromSpirit(ResourceLocation location) {
        return new ResourceTexture(location);
    }
}
