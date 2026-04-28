package com.lowdragmc.lowdraglib.jei;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ModularWrapper<T extends Widget> extends ModularUIGuiContainer {

    public final RecipeModularUI modularUI;
    protected T widget;
    protected boolean shouldRenderTooltips = true;
    private int left;
    private int top;

    public ModularWrapper(T widget) {
        super(createModularUI(widget), 0);
        this.modularUI = (RecipeModularUI) super.modularUI;
        this.widget = widget;
        setRecipeWidget(widget.getSelfPositionX(), widget.getSelfPositionY());
    }

    private static RecipeModularUI createModularUI(Widget widget) {
        var root = new WidgetGroup(0, 0, Math.max(widget.getSize().width, 1), Math.max(widget.getSize().height, 1));
        root.addWidget(widget);
        return new RecipeModularUI(root, IUIHolder.EMPTY, Minecraft.getInstance().player);
    }

    public String getUid() {
        return Integer.toHexString(System.identityHashCode(this));
    }

    public void setRecipeWidget(int left, int top) {
        this.left = left;
        this.top = top;
        widget.setSelfPosition(left, top);
        widget.setClientSideWidget();
        modularUI.setSize(left + widget.getSize().width, top + widget.getSize().height);
        if (!widget.isInitialized()) {
            widget.initWidget();
        }
        modularUI.toLDLib2().setAllowDebugMode(false);
        modularUI.toLDLib2().setDrawTooltips(false);
        modularUI.toLDLib2().init(modularUI.getWidth(), modularUI.getHeight());
    }

    public void setEmiRecipeWidget(int left, int top) {
        setRecipeWidget(left, top);
    }

    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        render(graphics, mouseX, mouseY, partialTicks);
    }

    public void updateScreen() {
        modularUI.mainGroup.updateScreen();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        updateScreen();
        modularUI.toLDLib2().ui.rootElement.drawInBackground(
                com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext.of(graphics, mouseX, mouseY, partialTicks));
        modularUI.mainGroup.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    public T getWidget() {
        return widget;
    }

    public boolean isShouldRenderTooltips() {
        return shouldRenderTooltips;
    }

    public void setShouldRenderTooltips(boolean shouldRenderTooltips) {
        this.shouldRenderTooltips = shouldRenderTooltips;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public static class RecipeModularUI extends ModularUI {

        public RecipeModularUI(WidgetGroup mainGroup, IUIHolder holder, Player entityPlayer) {
            super(mainGroup, holder, entityPlayer);
        }

        @Override
        public List<Widget> getFlatWidgetCollection() {
            return List.copyOf(super.getFlatWidgetCollection());
        }
    }
}
