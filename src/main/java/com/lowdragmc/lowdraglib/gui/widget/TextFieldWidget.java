package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextFieldWidget extends Widget implements IConfigurableWidget {

    private Supplier<String> textSupplier = () -> "";
    private Consumer<String> textResponder = value -> {};
    private String currentString = "";
    private int textColor = 0xFFFFFFFF;
    private Function<String, String> validator = Function.identity();

    public TextFieldWidget() {
        super(0, 0, 80, 16);
    }

    public TextFieldWidget(int x, int y, int width, int height, Supplier<String> textSupplier,
                           Consumer<String> textResponder) {
        super(x, y, width, height);
        this.textSupplier = textSupplier == null ? () -> currentString : textSupplier;
        this.textResponder = textResponder == null ? value -> {} : textResponder;
    }

    public TextFieldWidget setTextSupplier(Supplier<String> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public TextFieldWidget setTextResponder(Consumer<String> textResponder) {
        this.textResponder = textResponder;
        return this;
    }

    public TextFieldWidget setBackground(IGuiTexture texture) {
        super.setBackground(texture);
        return this;
    }

    public TextFieldWidget setCurrentString(Object value) {
        currentString = validator.apply(String.valueOf(value));
        textResponder.accept(currentString);
        return this;
    }

    public String getCurrentString() {
        return currentString;
    }

    public String getRawCurrentString() {
        return currentString;
    }

    @Override
    public void updateScreen() {
        if (textSupplier != null) currentString = textSupplier.get();
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(Minecraft.getInstance().font, currentString, getPositionX() + 2, getPositionY() + 2,
                textColor, false);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        setCurrentString(currentString + codePoint);
        return true;
    }

    public TextFieldWidget setBordered(boolean bordered) {
        return this;
    }

    public TextFieldWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public TextFieldWidget setMaxStringLength(int length) {
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidget setCompoundTagOnly() {
        return this;
    }

    public TextFieldWidget setResourceLocationOnly() {
        return this;
    }

    public TextFieldWidget setNumbersOnly(long min, long max) {
        return this;
    }

    public TextFieldWidget setNumbersOnly(int min, int max) {
        return this;
    }

    public TextFieldWidget setNumbersOnly(float min, float max) {
        return this;
    }

    public TextFieldWidget setWheelDur(float wheelDur) {
        return this;
    }

    public TextFieldWidget setWheelDur(int step, float wheelDur) {
        return this;
    }
}
