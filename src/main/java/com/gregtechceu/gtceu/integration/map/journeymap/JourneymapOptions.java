package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.layer.Layers;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;

import java.util.HashMap;
import java.util.Map;

public class JourneymapOptions {

    private final Map<String, BooleanOption> layerOptions = new HashMap<>();

    public JourneymapOptions() {
        final OptionCategory category = new OptionCategory(GTCEu.MOD_ID, "gtceu.journeymap.options.layers");

        for (String layerName : Layers.allKeys()) {
            final BooleanOption layer = new BooleanOption(category, layerName, "gtceu.button." + layerName, false);
            layerOptions.put(layerName, layer);
        }
    }

    public boolean showLayer(String name) {
        return layerOptions.get(name).get();
    }

    public void toggleLayer(String name, boolean active) {
        layerOptions.get(name).set(active);
        if (!active) {
            JourneymapRenderer.getMarkers().forEach((id, marker) -> {
                if (id.split("@")[0].equals(name)) {
                    IClientAPI api = JourneyMapPlugin.getJmApi();
                    api.remove(marker);
                }
            });
        } else {
            JourneymapRenderer.getMarkers().forEach((id, marker) -> {
                if (id.split("@")[0].equals(name)) {
                    try {
                        IClientAPI api = JourneyMapPlugin.getJmApi();
                        api.show(marker);
                    } catch (Exception e) {
                        // It never actually throws anything...
                        GTCEu.LOGGER.error("Failed to enable marker with name {}", name, e);
                    }
                }
            });
        }
    }
}
