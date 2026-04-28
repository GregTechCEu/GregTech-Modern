package com.lowdragmc.lowdraglib.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class LabelWidget extends Widget {

    protected Component component = Component.empty();
    protected Supplier<String> textProvider;
    protected int color = 0xFF404040;
    protected boolean dropShadow;

    public LabelWidget() {
        super(0, 0, 0, 10);
    }

    public LabelWidget(int x, int y, String text) {
        super(x, y, 0, 10);
        setText(text);
    }

    public LabelWidget(int x, int y, Component component) {
        super(x, y, 0, 10);
        setComponent(component);
    }

    public LabelWidget(int x, int y, Supplier<String> textProvider) {
        super(x, y, 0, 10);
        setTextProvider(textProvider);
    }

    public void setText(String text) {
        this.component = Component.translatable(text);
    }

    public void setTextProvider(Supplier<String> textProvider) {
        this.textProvider = textProvider;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public LabelWidget setTextColor(int color) {
        this.color = color;
        return this;
    }

    public LabelWidget setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {}

    @Override
    public void readInitialData(RegistryFriendlyByteBuf buffer) {}

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        String text = textProvider == null ? component.getString() : textProvider.get();
        graphics.drawString(Minecraft.getInstance().font, text, getPositionX(), getPositionY(), color, dropShadow);
    }

    public void setTextSupplier(Supplier<String> textProvider) {
        setTextProvider(textProvider);
    }
}
