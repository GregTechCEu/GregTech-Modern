package com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore;

import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

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
            ResourceKey<Level> currentDim = Minecraft.getInstance().level.dimension();
            this.iterator = XaerosRenderer.oreElements.row(currentDim).values().iterator();
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
