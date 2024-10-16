package com.gregtechceu.gtceu.integration.map.xaeros.minimap;

import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.map.WorldMap;

import java.util.Iterator;

public class OreVeinElementRenderProvider extends MinimapElementRenderProvider<OreVeinElement, OreVeinElementContext> {

    private Iterator<OreVeinElement> iterator;

    public OreVeinElementRenderProvider() {}

    @Override
    public void begin(MinimapElementRenderLocation location, OreVeinElementContext context) {
        if (WorldMap.settings.waypoints) {
            this.iterator = XaerosRenderer.oreElements.values().iterator();
            context.worldmapWaypointsScale = WorldMap.settings.worldmapWaypointsScale;
        } else {
            this.iterator = null;
        }
    }

    @Override
    public boolean hasNext(MinimapElementRenderLocation location, OreVeinElementContext context) {
        return this.iterator != null && this.iterator.hasNext();
    }

    @Override
    public OreVeinElement getNext(MinimapElementRenderLocation location, OreVeinElementContext context) {
        return this.iterator.next();
    }

    @Override
    public void end(MinimapElementRenderLocation location, OreVeinElementContext context) {}
}
