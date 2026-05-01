package com.lowdragmc.lowdraglib.emi;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;

import java.util.List;

public class ModularWrapperWidget extends Widget {

    public final ModularWrapper<?> modular;
    public final List<Widget> slots;
    private GuiEventListener focused;
    private boolean dragging;

    public ModularWrapperWidget(ModularWrapper<?> modular, List<Widget> slots) {
        this.modular = modular;
        this.slots = slots;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(modular.getLeft(), modular.getTop(), modular.getWidget().getSize().width,
                modular.getWidget().getSize().height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        modular.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return List.of();
    }

    public List<? extends GuiEventListener> children() {
        return List.of(modular);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return modular.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return mouseClicked((double) mouseX, (double) mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return modular.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseMoved(double mouseX, double mouseY) {
        modular.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return modular.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return modular.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return modular.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return modular.charTyped(codePoint, modifiers);
    }

    public GuiEventListener getFocused() {
        return focused;
    }

    public void setFocused(GuiEventListener focused) {
        this.focused = focused;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }
}
