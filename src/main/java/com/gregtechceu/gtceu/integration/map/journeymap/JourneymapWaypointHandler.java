package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.IWaypointHandler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import journeymap.common.api.waypoint.Waypoint;
import journeymap.common.api.waypoint.WaypointIcon;

import java.util.Collection;
import java.util.Map;

public class JourneymapWaypointHandler implements IWaypointHandler {

    private static final Map<String, Waypoint> waypoints = new Object2ObjectOpenHashMap<>();

    @Override
    public void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y, int z,
                            ResourceLocation texture) {
        waypoints.put(key, new Waypoint(new Waypoint.Builder(GTCEu.MOD_ID)
                .withName(name)
                .withPos(x, y, z)
                .withColorInt(color)
                .withDimension(dim)
                .withIcon(new WaypointIcon(texture))));
    }

    @Override
    public void removeWaypoint(String key) {
        waypoints.remove(key);
    }

    public static Collection<Waypoint> getWaypoints() {
        return waypoints.values();
    }
}
