package com.lowdragmc.lowdraglib.gui.texture;

public class GuiTextureGroup extends com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup implements IGuiTexture {

    public GuiTextureGroup() {}

    public GuiTextureGroup(IGuiTexture... textures) {
        super(textures);
    }

    public static GuiTextureGroup of(IGuiTexture... textures) {
        return new GuiTextureGroup(textures);
    }

    public GuiTextureGroup setTextures(IGuiTexture... textures) {
        super.setTextures(textures);
        return this;
    }

    @Override
    public GuiTextureGroup setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public GuiTextureGroup rotate(float degree) {
        super.rotate(degree);
        return this;
    }

    @Override
    public GuiTextureGroup scale(float scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public GuiTextureGroup transform(int xOffset, int yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public GuiTextureGroup transform(float xOffset, float yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public GuiTextureGroup copy() {
        return new GuiTextureGroup((IGuiTexture[]) getTextures());
    }
}
