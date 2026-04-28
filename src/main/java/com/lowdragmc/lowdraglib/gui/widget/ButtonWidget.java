package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import java.util.function.Consumer;

public class ButtonWidget extends Widget {

    protected Consumer<ClickData> onPressCallback;
    protected IGuiTexture buttonTexture = IGuiTexture.EMPTY;
    protected IGuiTexture clickedTexture = IGuiTexture.EMPTY;
    protected boolean clicked;

    public ButtonWidget() {
        super(0, 0, 10, 10);
    }

    public ButtonWidget(int x, int y, int width, int height, IGuiTexture texture,
                        Consumer<ClickData> onPressCallback) {
        super(x, y, width, height);
        setButtonTexture(texture);
        this.onPressCallback = onPressCallback;
    }

    public ButtonWidget(int x, int y, int width, int height, Consumer<ClickData> onPressCallback) {
        this(x, y, width, height, IGuiTexture.EMPTY, onPressCallback);
    }

    public void initTemplate() {}

    public ButtonWidget setOnPressCallback(Consumer<ClickData> onPressCallback) {
        this.onPressCallback = onPressCallback;
        return this;
    }

    public ButtonWidget setButtonTexture(IGuiTexture... textures) {
        buttonTexture = textures.length == 0 ? IGuiTexture.EMPTY : new GuiTextureGroup(textures);
        setBackground(buttonTexture);
        return this;
    }

    @Override
    public ButtonWidget setHoverTexture(IGuiTexture... textures) {
        super.setHoverTexture(textures);
        return this;
    }

    public ButtonWidget setClickedTexture(IGuiTexture... textures) {
        clickedTexture = textures.length == 0 ? IGuiTexture.EMPTY : new GuiTextureGroup(textures);
        return this;
    }

    public ButtonWidget kjs$setHoverTexture(IGuiTexture... textures) {
        return setHoverTexture(textures);
    }

    public ButtonWidget setHoverBorderTexture(int borderColor, int borderWidth) {
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY) || !isActive()) return false;
        clicked = true;
        if (onPressCallback != null) {
            onPressCallback.accept(new ClickData(button, isRemote()));
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        clicked = false;
        return isMouseOverElement(mouseX, mouseY);
    }

    public boolean isClicked() {
        return clicked;
    }
}
