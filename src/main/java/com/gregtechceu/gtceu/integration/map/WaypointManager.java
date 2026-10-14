package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksWaypointHandler;
import com.gregtechceu.gtceu.integration.map.journeymap.JourneymapWaypointHandler;
import com.gregtechceu.gtceu.integration.map.xaeros.XaeroWaypointHandler;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class WaypointManager {

    public static ResourceKey<Level> currentDimension;

    @Getter
    private static boolean active = false;

    public static void init() {
        var toggle = ConfigHolder.INSTANCE.compat.minimap.toggle;
        if (toggle.xaerosMapIntegration && GTCEu.isModLoaded(GTValues.MODID_XAEROS_MINIMAP)) {
            WaypointManager.registerWaypointHandler(new XaeroWaypointHandler());
            active = true;
        }
        if (toggle.journeyMapIntegration && GTCEu.isModLoaded(GTValues.MODID_JOURNEYMAP)) {
            WaypointManager.registerWaypointHandler(new JourneymapWaypointHandler());
            active = true;
        }
        if (toggle.ftbChunksIntegration && GTCEu.isModLoaded(GTValues.MODID_FTB_CHUNKS)) {
            WaypointManager.registerWaypointHandler(new FTBChunksWaypointHandler());
            active = true;
        }
    }

    private static final Set<IWaypointHandler> handlers = new HashSet<>();
    private static final Object2ObjectMap<String, WaypointKey> waypoints = new Object2ObjectArrayMap<>();

    public static void updateDimension(LevelAccessor dim) {
        if (dim instanceof ClientLevel level) {
            currentDimension = level.dimension();
        }
    }

    public static void setWaypoint(String key, String name, int color, ResourceKey<Level> dim, BlockPos pos) {
        if (dim == null) dim = currentDimension;
        for (IWaypointHandler handler : handlers) {
            handler.setWaypoint(key, name, color, dim, pos);
        }
        waypoints.put(key, new WaypointKey(dim, pos));
    }

    public static void removeWaypoint(String key) {
        for (IWaypointHandler handler : handlers) {
            handler.removeWaypoint(key);
        }
        waypoints.remove(key);
    }

    public static boolean toggleWaypoint(String key, String name, int color, ResourceKey<Level> dim, BlockPos pos) {
        if (dim == null) dim = currentDimension;
        if ((new WaypointKey(dim, pos)).equals(waypoints.get(key))) {
            removeWaypoint(key);
            return false;
        }
        setWaypoint(key, name, color, dim, pos);
        return true;
    }

    public static void registerWaypointHandler(IWaypointHandler handler) {
        handlers.add(handler);
    }

    private static class WaypointKey {

        ResourceKey<Level> dim;
        BlockPos pos;

        public WaypointKey(ResourceKey<Level> dim, BlockPos pos) {
            this.dim = dim;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WaypointKey that = (WaypointKey) o;
            return this.dim == that.dim && this.pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            return GTMath.hashInts(dim.hashCode(), pos.hashCode());
        }
    }
}
