package com.gregtechceu.gtceu.integration.map.ftbchunks;

import com.gregtechceu.gtceu.integration.map.IWaypointHandler;
import dev.ftb.mods.ftbchunks.api.client.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;

public class FTBChunksWaypointHandler implements IWaypointHandler {
    private static final Map<String, Waypoint> waypoints = new Object2ObjectOpenHashMap<>();
    
    @Override
    public void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y, int z) {
        var waypoint = FTBChunksIntegration.getWaypointManager().addWaypointAt(new BlockPos(x, y, z), name)
                .setColor(color)
                .setHidden(false);
        waypoints.put(key, waypoint);
    }

    @Override
    public void removeWaypoint(String key) {
        var removed = waypoints.remove(key);
        if (removed != null) {
            FTBChunksIntegration.getWaypointManager().removeWaypoint(removed);
        }
    }
}
