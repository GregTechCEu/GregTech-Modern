package com.lowdragmc.lowdraglib.gui.widget;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SwitchWidget extends Widget implements IConfigurableWidget {

    protected BiConsumer<ClickData, Boolean> onPressCallback = (clickData, value) -> {};
    protected IGuiTexture baseTexture = IGuiTexture.EMPTY;
    protected IGuiTexture pressedTexture = IGuiTexture.EMPTY;
    protected boolean pressed;
    protected boolean isPressed;
    protected Supplier<Boolean> supplier;

    public SwitchWidget() {
        super(0, 0, 20, 20);
    }

    public SwitchWidget(int x, int y, int width, int height, BiConsumer<ClickData, Boolean> onPressCallback) {
        super(x, y, width, height);
        this.onPressCallback = onPressCallback;
    }

    public void initTemplate() {}

    public void setOnPressCallback(BiConsumer<ClickData, Boolean> onPressCallback) {
        this.onPressCallback = onPressCallback;
    }

    public SwitchWidget setTexture(IGuiTexture baseTexture, IGuiTexture pressedTexture) {
        this.baseTexture = baseTexture;
        this.pressedTexture = pressedTexture;
        return this;
    }

    public SwitchWidget setBaseTexture(IGuiTexture... textures) {
        baseTexture = new GuiTextureGroup(textures);
        return this;
    }

    public SwitchWidget setPressedTexture(IGuiTexture... textures) {
        pressedTexture = new GuiTextureGroup(textures);
        return this;
    }

    @Override
    public SwitchWidget setHoverTexture(IGuiTexture... textures) {
        super.setHoverTexture(textures);
        return this;
    }

    public SwitchWidget setHoverBorderTexture(int borderColor, int borderWidth) {
        return this;
    }

    public boolean isPressed() {
        return supplier == null ? pressed : supplier.get();
    }

    public SwitchWidget setPressed(boolean pressed) {
        this.pressed = pressed;
        this.isPressed = pressed;
        return this;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        (isPressed() ? pressedTexture : baseTexture).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(),
                getSizeWidth(), getSizeHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) return false;
        pressed = !pressed;
        isPressed = pressed;
        onPressCallback.accept(new ClickData(button, isRemote()), pressed);
        return true;
    }

    public SwitchWidget setSupplier(Supplier<Boolean> supplier) {
        this.supplier = supplier;
        return this;
    }
}
