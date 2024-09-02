package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.IWaypointHandler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.util.Hashtable;
import java.util.List;

public class XaeroWaypointHandler implements IWaypointHandler {

    private final Hashtable<Integer, Waypoint> xwaypoints = WaypointsManager.getCustomWaypoints(GTCEu.MOD_ID);
    private final List<String> knownKeys = new ObjectArrayList<>();

    @Override
    public void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y, int z,
                            ResourceLocation texture) {
        xwaypoints.put(getIndex(key), new WaypointWithDimension(dim, x, y, z, name, name.substring(0, 1), 15));
    }

    @Override
    public void removeWaypoint(String key) {
        xwaypoints.remove(getIndex(key));
    }

    private int getIndex(String key) {
        if (!knownKeys.contains(key)) {
            knownKeys.add(key);
        }
        return knownKeys.indexOf(key);
    }
}
