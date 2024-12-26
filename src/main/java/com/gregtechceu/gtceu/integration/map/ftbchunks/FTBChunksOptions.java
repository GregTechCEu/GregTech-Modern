package com.gregtechceu.gtceu.integration.map.ftbchunks;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;

import java.util.HashMap;
import java.util.Map;

import static dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig.CONFIG;

public class FTBChunksOptions {

    private final Map<String, BooleanValue> layerOptions = new HashMap<>();

    private final BooleanValue hideDepleted;

    public FTBChunksOptions() {
        var group = CONFIG.addGroup("gtceu_prospecting");
        final var oreLayer = group.addBoolean("ore_veins", false);
        layerOptions.put(oreLayer.key, oreLayer);
        final var fluidLayer = group.addBoolean("bedrock_fluids", false);
        layerOptions.put(fluidLayer.key, fluidLayer);
        hideDepleted = group.addBoolean("hide_depleted", false);
    }

    public boolean showLayer(String name) {
        return layerOptions.get(name).get();
    }

    public void toggleLayer(String name, boolean active) {
        layerOptions.get(name).set(active);
        FTBChunksPlugin.getInstance().clientAPI.getWaypointManager().ifPresent(manager -> {
            manager.getAllWaypoints().forEach(waypoint -> {
                if (waypoint.getName().equals(name)) {
                    waypoint.setHidden(!active);
                }
            });
        });
    }

    public boolean hideDepleted() {
        return hideDepleted.get();
    }
}
