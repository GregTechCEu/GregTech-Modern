package com.gregtechceu.gtceu.integration.map.ftbchunks;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;

import java.util.HashMap;
import java.util.Map;

import static dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig.CONFIG;

public class FTBChunksOptions {

    private static final Map<String, BooleanValue> layerOptions = new HashMap<>();

    private static BooleanValue hideDepleted;

    private FTBChunksOptions() {}

    public static void initialize() {
        var group = CONFIG.addGroup("gtceu_prospecting");
        var oreLayer = group.addBoolean("ore_veins", false);
        layerOptions.put(oreLayer.key, oreLayer);
        var fluidLayer = group.addBoolean("bedrock_fluids", false);
        layerOptions.put(fluidLayer.key, fluidLayer);
        hideDepleted = group.addBoolean("hide_depleted", false);
    }

    public static boolean showLayer(String name) {
        return layerOptions.get(name).get();
    }

    public static void toggleLayer(String name, boolean active) {
        layerOptions.get(name).set(active);
        FTBChunksClientConfig.saveConfig();
        FTBChunksAPI.clientApi().getWaypointManager().ifPresent(manager -> {
            manager.getAllWaypoints().forEach(waypoint -> {
                if (waypoint.getName().equals(name)) {
                    waypoint.setHidden(!active);
                }
            });
        });
    }

    public static boolean hideDepleted() {
        return hideDepleted.get();
    }
}
