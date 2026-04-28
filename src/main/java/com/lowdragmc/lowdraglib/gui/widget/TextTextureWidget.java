package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextTextureWidget extends Widget implements IConfigurableWidget {

    private Component lastComponent = Component.empty();
    private final TextTexture textTexture = new TextTexture("");
    private Supplier<Component> textSupplier = () -> lastComponent;

    public TextTextureWidget() {
        this(0, 0, 80, 15);
    }

    public TextTextureWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public TextTextureWidget(int x, int y, int width, int height, String text) {
        this(x, y, width, height);
        setText(text);
    }

    public void setLastComponent(Component lastComponent) {
        this.lastComponent = lastComponent;
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        textTexture.setWidth(size.width);
    }

    public TextTextureWidget textureStyle(Consumer<TextTexture> consumer) {
        consumer.accept(textTexture);
        return this;
    }

    public TextTextureWidget setText(String text) {
        return setText(Component.literal(text));
    }

    public TextTextureWidget setText(Component component) {
        this.lastComponent = component;
        this.textSupplier = () -> component;
        return this;
    }

    public TextTextureWidget setText(Supplier<Component> supplier) {
        this.textSupplier = supplier;
        return this;
    }

    @Override
    public void updateScreen() {
        lastComponent = textSupplier.get();
        textTexture.updateText(lastComponent.getString());
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        updateScreen();
        textTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }

    public Component getLastComponent() {
        return lastComponent;
    }

    public TextTexture getTextTexture() {
        return textTexture;
    }
}
