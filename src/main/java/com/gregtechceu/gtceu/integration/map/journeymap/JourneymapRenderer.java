package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A map renderer for Journeymap, uses Journeymap's own tooltip rendering to fit existing theming better
 */
public class JourneymapRenderer extends GenericMapRenderer {
    public JourneymapRenderer(Fullscreen gui) {
        super(gui);
    }

    @Override
    protected void renderTooltipInternal(List<Component> tooltip, GuiGraphics graphics, int mouseX, int mouseY) {
        ((Fullscreen) gui).renderWrappedToolTip(graphics, tooltip.stream().map(Component::getVisualOrderText).toList(),
                mouseX, mouseY, ((Fullscreen) gui).getFontRenderer());
    }
}
