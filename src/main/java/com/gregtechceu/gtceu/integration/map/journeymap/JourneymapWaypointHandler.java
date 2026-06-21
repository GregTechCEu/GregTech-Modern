package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.IWaypointHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;

import java.util.Map;

public class JourneymapWaypointHandler implements IWaypointHandler {

    private static final Map<String, Waypoint> waypoints = new Object2ObjectOpenHashMap<>();

    @Override
    public void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y, int z) {
        Waypoint waypoint = WaypointFactory.createWaypoint(GTCEu.MOD_ID, new BlockPos(x, y, z), name, dim, true);
        waypoint.setColor(color);
        waypoints.put(key, waypoint);
        try {
            GTJourneyMapPlugin.getJmApi().addWaypoint(GTCEu.MOD_ID, waypoint);
        } catch (Exception e) {
            // It never actually throws anything...
            GTCEu.LOGGER.error("Failed to enable waypoint with name {}", name, e);
        }
    }

    @Override
    public void removeWaypoint(String key) {
        Waypoint removed = waypoints.remove(key);
        if (removed != null) {
            GTJourneyMapPlugin.getJmApi().removeWaypoint(GTCEu.MOD_ID, removed);
        }
    }
}
