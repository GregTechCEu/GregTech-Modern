package com.lowdragmc.lowdraglib.gui.modular;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModularUIGuiContainer extends AbstractContainerScreen<ModularUIContainer> {

    public final ModularUI modularUI;
    public Widget lastFocus;
    public boolean focused;
    public int dragSplittingLimit;
    public int dragSplittingButton;
    public List<Component> tooltipTexts = List.of();
    public TooltipComponent tooltipComponent;
    public Font tooltipFont;
    public ItemStack tooltipStack = ItemStack.EMPTY;
    private Object draggingElement;

    public ModularUIGuiContainer(ModularUI modularUI, int containerId) {
        super(modularUI.getModularUIContainer(), modularUI.entityPlayer.getInventory(), Component.empty());
        this.modularUI = modularUI;
        modularUI.setModularUIGui(this);
    }

    public void setHoverTooltip(List<Component> tooltipTexts, ItemStack tooltipStack, Font tooltipFont,
                                TooltipComponent tooltipComponent) {
        this.tooltipTexts = tooltipTexts;
        this.tooltipStack = tooltipStack;
        this.tooltipFont = tooltipFont;
        this.tooltipComponent = tooltipComponent;
    }

    public boolean setDraggingElement(Object draggingElement, IGuiTexture texture) {
        this.draggingElement = draggingElement;
        return true;
    }

    public Object getDraggingElement() {
        return draggingElement;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        modularUI.mainGroup.drawInBackground(graphics, mouseX, mouseY, partialTick);
        modularUI.mainGroup.drawInForeground(graphics, mouseX, mouseY, partialTick);
    }

    public boolean switchFocus(Widget widget) {
        lastFocus = widget;
        focused = widget != null;
        return true;
    }

    public Set<Slot> getQuickCraftSlots() {
        return Set.of();
    }

    public boolean getQuickCrafting() {
        return false;
    }

    public boolean isButtonPressed(int button) {
        return minecraft != null && minecraft.mouseHandler.isLeftPressed();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return modularUI.mainGroup.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return modularUI.mainGroup.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return modularUI.mainGroup.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public void mouseMoved(double mouseX, double mouseY) {
        modularUI.mainGroup.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return modularUI.mainGroup.mouseWheelMove(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return modularUI.mainGroup.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return modularUI.mainGroup.charTyped(codePoint, modifiers);
    }

    public void superMouseClicked(double mouseX, double mouseY, int button) {}

    public void superMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {}

    public void superMouseReleased(double mouseX, double mouseY, int button) {}

    public boolean superKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean superMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    public boolean superKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean superCharTyped(char codePoint, int modifiers) {
        return false;
    }

    public void superMouseMoved(double mouseX, double mouseY) {}

    public List<Rect2i> getGuiExtraAreas() {
        return new ArrayList<>();
    }
}
