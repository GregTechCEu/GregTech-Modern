package com.gregtechceu.gtceu.client.mui.component;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextIcon;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.NotNull;

public class DrawableTooltipComponent implements ClientTooltipComponent, TooltipComponent {

    private final IDrawable drawable;

    public DrawableTooltipComponent(IDrawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public int getHeight() {
        if (drawable instanceof TextIcon text) {
            return text.getHeight();
        }
        if (drawable instanceof IIcon icon) {
            return icon.getHeight();
        } else if (!(drawable instanceof IKey key)) {
            return 18;
        } else {
            return key.asTextIcon().getHeight();
        }
    }

    @Override
    public int getWidth(@NotNull Font font) {
        if (drawable instanceof TextIcon text) {
            return text.getWidth();
        }
        if (drawable instanceof IIcon icon) {
            return icon.getWidth();
        } else if (!(drawable instanceof IKey key)) {
            return 18;
        } else {
            return key.asTextIcon().getWidth();
        }
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
        GuiContext context = GuiContext.getDefault();
        GuiGraphics lastGraphics = context.getGraphics();

        context.setGraphics(guiGraphics);
        drawable.draw(context, x, y, getWidth(font), getHeight(), WidgetTheme.getDefault());
        context.setGraphics(lastGraphics);
    }
}
