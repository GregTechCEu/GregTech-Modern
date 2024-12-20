package com.gregtechceu.gtceu.integration.map.ftbchunks;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.FTBChunksClientAPI;
import dev.ftb.mods.ftbchunks.api.client.event.MapIconEvent;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
import dev.ftb.mods.ftbchunks.api.client.waypoint.Waypoint;
import dev.ftb.mods.ftbchunks.api.client.waypoint.WaypointManager;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.MapManager;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class FTBChunksIntegration {

    @Getter
    private static FTBChunksClientAPI clientAPI;

    @Getter
    private static WaypointManager waypointManager;
    
    @Getter
    private static FTBChunksOptions options;

    public static void init() {
        options = new FTBChunksOptions();
        clientAPI = FTBChunksAPI.clientApi();
        if (clientAPI.getWaypointManager().isPresent()) {
            waypointManager = clientAPI.getWaypointManager().get();
        } else {
            waypointManager = new WaypointManager() {
                @Override
                public Waypoint addWaypointAt(BlockPos blockPos, String s) {
                    return null;
                }

                @Override
                public boolean removeWaypointAt(BlockPos blockPos) {
                    return false;
                }

                @Override
                public boolean removeWaypoint(Waypoint waypoint) {
                    return false;
                }

                @Override
                public Collection<Waypoint> getAllWaypoints() {
                    return List.of();
                }

                @Override
                public Optional<? extends Waypoint> getNearestDeathpoint(Player player) {
                    return Optional.empty();
                }
            };
        }
        MapIconEvent.MINIMAP.register(FTBChunksIntegration::onMapIconEvent);
        MapIconEvent.LARGE_MAP.register(FTBChunksIntegration::onMapIconEvent);
    }

    private static void onMapIconEvent(MapIconEvent event) {
        if (event.getMapType() == MapType.MINIMAP) {
            // todo: check if point is within render distance before adding
        }
        
    }

    public static Optional<MapManager> getMapManager() {
        return MapManager.getInstance();
    }
    
    public static Optional<MapDimension> getMapDimension(ResourceKey<Level> dimension) {
        return getMapManager().map(manager -> manager.getDimension(dimension));
    }
}
