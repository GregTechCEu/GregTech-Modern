package com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.client.Minecraft;

import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.map.WorldMap;

public class OreVeinElementReader extends MinimapElementReader<OreVeinElement, OreVeinElementContext> {

    @Override
    public boolean isHidden(OreVeinElement element, OreVeinElementContext context) {
        return !GroupingMapRenderer.getInstance().doShowLayer("ore_veins");
    }

    @Override
    public double getRenderX(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return element.getVein().center().getX();
    }

    @Override
    public double getRenderY(OreVeinElement var1, OreVeinElementContext var2, float var3) {
        return 0;
    }

    @Override
    public double getRenderZ(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return element.getVein().center().getZ();
    }

    @Override
    public int getInteractionBoxLeft(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return -this.getInteractionBoxRight(element, context, partialTicks);
    }

    @Override
    public int getInteractionBoxRight(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return ConfigHolder.INSTANCE.compat.minimap.oreIconSize;
    }

    @Override
    public int getInteractionBoxTop(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return WorldMap.settings.waypointBackgrounds ? -41 : -12;
    }

    @Override
    public int getInteractionBoxBottom(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return WorldMap.settings.waypointBackgrounds ? 0 : 12;
    }

    @Override
    public int getLeftSideLength(OreVeinElement element, Minecraft mc) {
        return 9 + element.getCachedNameLength();
    }

    @Override
    public String getMenuName(OreVeinElement element) {
        return element.getName();
    }

    @Override
    public int getMenuTextFillLeftPadding(OreVeinElement element) {
        return 0;
    }

    @Override
    public String getFilterName(OreVeinElement element) {
        return this.getMenuName(element);
    }

    @Override
    public int getRightClickTitleBackgroundColor(OreVeinElement element) {
        return element.getFirstMaterial().getMaterialRGB();
    }

    @Override
    public boolean shouldScaleBoxWithOptionalScale() {
        return true;
    }

    @Override
    public int getRenderBoxLeft(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        int left = this.getInteractionBoxLeft(element, context, partialTicks);
        return Math.min(left, -element.getCachedNameLength() * 3 / 2);
    }

    @Override
    public int getRenderBoxRight(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        int right = this.getInteractionBoxRight(element, context, partialTicks) + 12;
        return Math.max(right, element.getCachedNameLength() * 3 / 2);
    }

    @Override
    public int getRenderBoxTop(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return this.getInteractionBoxTop(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxBottom(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return this.getInteractionBoxBottom(element, context, partialTicks);
    }
}
