package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VerticalTabsWidget extends TabsWidget {

    public VerticalTabsWidget(Consumer<IFancyUIProvider> onTabClick, int x, int y, int width, int height) {
        super(onTabClick, x, y, width, height);
        ResourceTexture tabsLeft = new ResourceTexture("gtceu:textures/gui/tab/tabs_left.png");
        setTabTexture(tabsLeft.getSubTexture(0, 1 / 3f, 0.5f, 1 / 3f));
        setTabHoverTexture(tabsLeft.getSubTexture(0.5f, 1 / 3f, 0.5f, 1 / 3f));
        setTabPressedTexture(tabsLeft.getSubTexture(0.5f, 1 / 3f, 0.5f, 1 / 3f));
    }

    @Override
    public boolean hasButton() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
        var position = getPosition();
        var size = getSize();
        var hoveredTab = getHoveredTab(mouseX, mouseY);
        // main tab
        drawTab(mainTab, graphics, mouseX, mouseY, position.x, position.y + 8, 24, 24, hoveredTab);
        for (int i = 0; i < subTabs.size(); ++i) {
            drawTab(subTabs.get(i), graphics, mouseX, mouseY, position.x, position.y + 8 + 24 * (i + 1), 24, 24,
                    hoveredTab);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public IFancyUIProvider getHoveredTab(double mouseX, double mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var position = getPosition();
            var size = getSize();
            // main tab
            if (isMouseOver(position.x, position.y + 8, 24, 24, mouseX, mouseY)) {
                return mainTab;
            }
            // others
            int i = ((int) mouseY - position.y - 24 - 8) / 24;
            if (i >= 0 && i < subTabs.size()) {
                return subTabs.get(i);
            }
        }
        return null;
    }
}
