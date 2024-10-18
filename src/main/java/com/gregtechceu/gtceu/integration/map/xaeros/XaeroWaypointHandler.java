package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.IWaypointHandler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.waypoint.WaypointColor;

import java.util.List;

public class XaeroWaypointHandler implements IWaypointHandler {

    private final Lazy<Int2ObjectMap<Waypoint>> waypoints = Lazy.of(() -> BuiltInHudModules.MINIMAP.getCurrentSession()
            .getWorldManager()
            .getCustomWaypoints(GTCEu.id(GTCEu.MOD_ID)));
    private final List<String> knownKeys = new ObjectArrayList<>();

    @Override
    public void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, int x, int y, int z) {
        waypoints.get().put(getIndex(key),
                new WaypointWithDimension(dim, x, y, z, name, name.substring(0, 1), WaypointColor.WHITE));
    }

    @Override
    public void removeWaypoint(String key) {
        waypoints.get().remove(getIndex(key));
    }

    private int getIndex(String key) {
        if (!knownKeys.contains(key)) {
            knownKeys.add(key);
        }
        return knownKeys.indexOf(key);
    }
}
