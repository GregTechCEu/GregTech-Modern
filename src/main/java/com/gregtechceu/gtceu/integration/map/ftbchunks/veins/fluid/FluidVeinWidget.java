package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import net.minecraft.client.gui.GuiGraphics;

import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import lombok.Getter;

public class FluidVeinWidget extends Widget {

    @Getter
    private final FluidVeinIcon mapIcon;

    public FluidVeinWidget(RegionMapPanel p, FluidVeinIcon mapIcon) {
        super(p);
        this.mapIcon = mapIcon;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (!shouldDraw()) {
            return;
        }
        mapIcon.getIcon(isMouseOver() ? 230 : 205, isMouseOver()).draw(graphics, x, y, w, h);
    }
}
