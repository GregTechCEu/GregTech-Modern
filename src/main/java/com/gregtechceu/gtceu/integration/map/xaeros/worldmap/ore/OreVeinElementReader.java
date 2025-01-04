package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import xaero.map.WorldMap;
import xaero.map.element.MapElementReader;
import xaero.map.gui.CursorBox;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OreVeinElementReader extends
                                  MapElementReader<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> {

    @Override
    public boolean isHidden(OreVeinElement element, OreVeinElementContext context) {
        return !GroupingMapRenderer.getInstance().doShowLayer("ore_veins");
    }

    @Override
    public boolean isInteractable(int location, OreVeinElement element) {
        return true;
    }

    @Override
    public float getBoxScale(int location, OreVeinElement element, OreVeinElementContext context) {
        return context.worldmapWaypointsScale;
    }

    @Override
    public double getRenderX(OreVeinElement element, OreVeinElementContext context, float partialTicks) {
        return element.getVein().center().getX();
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
    public ArrayList<RightClickOption> getRightClickOptions(final OreVeinElement element,
                                                            IRightClickableElement target) {
        ArrayList<RightClickOption> rightClickOptions = new ArrayList<>();
        rightClickOptions.add(new RightClickOption("button.gtceu.toggle_waypoint.name", 0, target) {

            @Override
            public void onAction(Screen screen) {
                element.onMouseSelect();
            }
        });
        rightClickOptions
                .add((new RightClickOption("button.gtceu.mark_as_depleted.name", rightClickOptions.size(), target) {

                    @Override
                    public void onAction(Screen screen) {
                        element.toggleDepleted();
                    }
                }));
        return rightClickOptions;
    }

    @Override
    public boolean isRightClickValid(OreVeinElement element) {
        return true;
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

    @Override
    public CursorBox getTooltip(OreVeinElement element, OreVeinElementContext context, boolean overMenu) {
        List<Component> components = OreRenderLayer.getTooltip(element.getName(), element.getVein());
        // Xaeros requires spaces before/after newlines (see xaero.map.misc.TextSplitter)
        String joined = components.stream().map(Component::getString).collect(Collectors.joining(" \n "));
        return new CursorBox(joined);
    }
}
