package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TooltipsPanel extends Widget {

    @Getter
    protected List<IFancyTooltip> tooltips = new ArrayList<>();

    public TooltipsPanel() {
        super(202, 2, 20, 0);
    }

    public void clear() {
        tooltips.clear();
    }

    public void attachTooltips(IFancyTooltip... tooltips) {
        this.tooltips.addAll(Arrays.asList(tooltips));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var position = getPosition();
        var size = getSize();
        int offsetY = 0;
        for (IFancyTooltip tooltip : this.tooltips) {
            if (tooltip.showFancyTooltip()) {
                // draw icon
                tooltip.getFancyTooltipIcon().draw(graphics, mouseX, mouseY, position.x, position.y + offsetY,
                        size.width, size.width);
                offsetY += size.getWidth() + 2;
            }
        }
        setSize(new Size(getSize().width, Math.max(0, offsetY)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (getHoverElement(mouseX, mouseY) == this && gui != null && gui.getModularUIGui() != null) {
            var position = getPosition();
            var size = getSize();
            int offsetY = 0;
            for (IFancyTooltip tooltip : this.tooltips) {
                if (tooltip.showFancyTooltip()) {
                    if (isMouseOver(position.x, position.y + offsetY, size.width, size.width, mouseX, mouseY)) {
                        gui.getModularUIGui().setHoverTooltip(tooltip.getFancyTooltip(), ItemStack.EMPTY, null,
                                tooltip.getFancyComponent());
                        return;
                    }
                    offsetY += size.getWidth() + 2;
                }
            }
        }
    }
}
