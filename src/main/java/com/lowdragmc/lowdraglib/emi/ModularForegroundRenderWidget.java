package com.lowdragmc.lowdraglib.emi;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;

public class ModularForegroundRenderWidget extends Widget {

    public final ModularWrapper<?> modular;

    public ModularForegroundRenderWidget(ModularWrapper<?> modular) {
        this.modular = modular;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(modular.getLeft(), modular.getTop(), modular.getWidget().getSize().width,
                modular.getWidget().getSize().height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (modular.isShouldRenderTooltips()) {
            var hover = modular.getWidget().getHoverElement(mouseX, mouseY);
            if (hover != null && modular.modularUI.getModularUIGui() != null) {
                modular.modularUI.getModularUIGui().setHoverTooltip(hover.getTooltipTexts(),
                        net.minecraft.world.item.ItemStack.EMPTY, null, null);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {}
}
