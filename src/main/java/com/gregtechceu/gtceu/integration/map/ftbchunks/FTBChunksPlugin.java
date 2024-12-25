package com.gregtechceu.gtceu.integration.map.ftbchunks;

import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.FTBChunksClientAPI;
import dev.ftb.mods.ftbchunks.api.client.event.MapIconEvent;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import lombok.Getter;

public class FTBChunksPlugin {

    private static FTBChunksPlugin instance;

    @Getter
    protected FTBChunksClientAPI clientAPI;
    @Getter
    protected FTBChunksOptions options;

    protected FTBChunksPlugin() {}

    public static FTBChunksPlugin getInstance() {
        return instance == null ? instance = new FTBChunksPlugin() : instance;
    }

    public void initialize() {
        options = new FTBChunksOptions();
        clientAPI = FTBChunksAPI.clientApi();

        MapIconEvent.MINIMAP.register(this::mapIconEventHandler);
        MapIconEvent.LARGE_MAP.register(this::mapIconEventHandler);
    }

    private void mapIconEventHandler(MapIconEvent event) {
        if (!GroupingMapRenderer.getInstance().doShowLayer("ore_veins")) return;
        MapDimension.getCurrent().ifPresent(
                mapDimension -> FTBChunksRenderer.oreElements.row(mapDimension.dimension).values().forEach(event::add));
    }
}
