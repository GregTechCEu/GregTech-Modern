package com.gregtechceu.gtceu.integration.map.xaeros;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;

public class WaypointWithDimension extends Waypoint {

    private final ResourceKey<Level> dim;

    public WaypointWithDimension(ResourceKey<Level> dim, int x, int y, int z, String name, String symbol,
                                 WaypointColor color) {
        super(x, y, z, name, symbol, color);
        this.dim = dim;
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !dim.equals(Minecraft.getInstance().level.dimension());
    }
}
