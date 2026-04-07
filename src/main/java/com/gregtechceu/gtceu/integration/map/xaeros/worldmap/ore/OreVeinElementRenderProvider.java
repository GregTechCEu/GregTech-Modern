package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore;

import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import xaero.map.WorldMap;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.element.MapElementRenderProvider;

import java.util.Iterator;

public class OreVeinElementRenderProvider extends MapElementRenderProvider<OreVeinElement, OreVeinElementContext> {

    private Iterator<OreVeinElement> iterator;

    public OreVeinElementRenderProvider() {}

    public void begin(int location, OreVeinElementContext context) {
        if (WorldMap.INSTANCE.getConfigs().getClientConfigManager().getEffective(
                WorldMapProfiledConfigOptions.WAYPOINT_BACKGROUNDS)) {
            ResourceKey<Level> currentDim = Minecraft.getInstance().level.dimension();
            this.iterator = XaerosRenderer.oreElements.row(currentDim).values()
                    .stream()
                    .map(element -> new OreVeinElement(element.getVein(), element.getName()))
                    .iterator();
            context.worldmapWaypointsScale = WorldMap.INSTANCE.getConfigs().getClientConfigManager()
                    .getEffective(WorldMapProfiledConfigOptions.WAYPOINT_SCALE).floatValue();
        } else {
            this.iterator = null;
        }
    }

    public boolean hasNext(int location, OreVeinElementContext context) {
        return this.iterator != null && this.iterator.hasNext();
    }

    public OreVeinElement getNext(int location, OreVeinElementContext context) {
        return this.iterator.next();
    }

    public void end(int location, OreVeinElementContext context) {}
}
