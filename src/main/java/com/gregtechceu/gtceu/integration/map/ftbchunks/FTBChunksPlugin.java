package com.gregtechceu.gtceu.integration.map.ftbchunks;

import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import dev.ftb.mods.ftbchunks.api.client.event.MapIconEvent;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;

public class FTBChunksPlugin {

    private FTBChunksPlugin() {}

    public static void addEventListeners() {
        MapIconEvent.MINIMAP.register(FTBChunksPlugin::mapIconEventHandler);
        MapIconEvent.LARGE_MAP.register(FTBChunksPlugin::mapIconEventHandler);
    }

    private static void mapIconEventHandler(MapIconEvent event) {
        if (GroupingMapRenderer.getInstance().doShowLayer("ore_veins")) {
            MapDimension.getCurrent()
                    .ifPresent(mapDimension -> FTBChunksRenderer.oreElements.row(mapDimension.dimension)
                            .values()
                            .forEach(event::add));
        }
    }
}
