package com.gregtechceu.gtceu.integration.map.ftbchunks;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;

import java.util.HashMap;
import java.util.Map;

public class FTBChunksOptions {

    private static final Map<String, BooleanValue> layerOptions = new HashMap<>();

    private static final SNBTConfig CONFIG = SNBTConfig.create("gtceu");
    private static final SNBTConfig LAYERS = CONFIG.addGroup("journeymap.options.layers");
    private static final BooleanValue ORE_LAYER = LAYERS.addBoolean("ore_veins", false);
    private static final BooleanValue FLUID_LAYER = LAYERS.addBoolean("bedrock_fluids", false);
    private static final BooleanValue HIDE_DEPLETED = LAYERS.addBoolean("hide_depleted", false);

    static {
        layerOptions.put(ORE_LAYER.key, ORE_LAYER);
        layerOptions.put(FLUID_LAYER.key, FLUID_LAYER);
        layerOptions.put(HIDE_DEPLETED.key, HIDE_DEPLETED);

        loadConfig();
    }

    private FTBChunksOptions() {}

    public static boolean showLayer(String name) {
        return layerOptions.get(name).get();
    }

    public static void toggleLayer(String name, boolean active) {
        layerOptions.get(name).set(active);
        saveConfig();
        FTBChunksAPI.clientApi().getWaypointManager()
                .ifPresent(manager -> manager.getAllWaypoints().forEach(waypoint -> {
                    if (waypoint.getName().equals(name)) {
                        waypoint.setHidden(!active);
                    }
                }));
    }

    public static boolean hideDepleted() {
        return HIDE_DEPLETED.get();
    }

    public static void loadConfig() {
        ConfigUtil.loadDefaulted(CONFIG, ConfigUtil.LOCAL_DIR.resolve("gtceu"), "gtceu", "client-config.snbt");
    }

    public static void saveConfig() {
        CONFIG.save(ConfigUtil.LOCAL_DIR.resolve("gtceu").resolve("client-config.snbt"));
    }
}
