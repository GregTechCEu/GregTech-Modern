package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.util.ClickData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ComponentPanelWidget extends Widget {

    private Consumer<List<Component>> textSupplier;
    private BiConsumer<String, ClickData> clickHandler = (button, clickData) -> {};
    private final List<Component> components = new ArrayList<>();
    private int maxWidthLimit = 160;
    private boolean center;
    private int space = 10;

    public ComponentPanelWidget(int x, int y, Consumer<List<Component>> textSupplier) {
        super(x, y, 160, 10);
        this.textSupplier = textSupplier;
    }

    public ComponentPanelWidget(int x, int y, List<Component> components) {
        super(x, y, 160, 10);
        this.components.addAll(components);
    }

    public static Component withButton(Component component, String id) {
        return component;
    }

    public static Component withButton(Component component, String id, int color) {
        return component;
    }

    public static Component withHoverTextTranslate(Component component, Component hover) {
        return component;
    }

    public ComponentPanelWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        return this;
    }

    public ComponentPanelWidget setCenter(boolean center) {
        this.center = center;
        return this;
    }

    public ComponentPanelWidget setSpace(int space) {
        this.space = space;
        return this;
    }

    @Override
    public void updateScreen() {
        if (textSupplier != null) {
            components.clear();
            textSupplier.accept(components);
        }
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        updateScreen();
        int y = getPositionY();
        for (Component component : components) {
            graphics.drawString(Minecraft.getInstance().font, component.getString(), getPositionX(), y, 0xFFFFFFFF,
                    false);
            y += space;
        }
    }

    public ComponentPanelWidget textSupplier(Consumer<List<Component>> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public ComponentPanelWidget clickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    public List<FormattedCharSequence> cacheLines() {
        return components.stream().map(Component::getVisualOrderText).toList();
    }
}
