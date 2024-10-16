package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.journeymap.JourneymapRenderer;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import com.lowdragmc.lowdraglib.Platform;

import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * A map renderer that groups together multiple map mods' renderers for supporting multiple simultaneous mods.
 */
public class GroupingMapRenderer extends GenericMapRenderer {

    @Getter
    private static final GroupingMapRenderer instance;

    static {
        Map<String, GenericMapRenderer> renderers = new HashMap<>();
        if (Platform.isModLoaded(GTValues.MODID_JOURNEYMAP)) {
            renderers.put(GTValues.MODID_JOURNEYMAP, new JourneymapRenderer());
        }
        if (Platform.isModLoaded(GTValues.MODID_XAEROS_MINIMAP)) {
            renderers.put(GTValues.MODID_XAEROS_MINIMAP, new XaerosRenderer());
        }
        if (Platform.isModLoaded(GTValues.MODID_FTB_CHUNKS)) {
            // TODO FTB chunks support
        }

        instance = new GroupingMapRenderer(renderers);
    }

    @Getter
    private final Map<String, GenericMapRenderer> renderers;
    private final GenericMapRenderer[] rendererList;

    public GroupingMapRenderer(Map<String, GenericMapRenderer> renderers) {
        super(false);
        this.renderers = renderers;
        this.rendererList = renderers.values().toArray(GenericMapRenderer[]::new);
    }

    @Override
    public boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos, ProspectorMode.FluidInfo fluid) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.addMarker(name, id, dim, pos, fluid);
        }
        return value;
    }

    @Override
    public boolean addMarker(String name, String id, GeneratedVeinMetadata vein) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.addMarker(name, id, vein);
        }
        return value;
    }

    @Override
    public boolean removeMarker(String id) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.removeMarker(id);
        }
        return value;
    }

    @Override
    public boolean doShowLayer(String name) {
        boolean value = false;
        for (GenericMapRenderer renderer : rendererList) {
            value |= renderer.doShowLayer(name);
        }
        return value;
    }

    @Override
    public void setLayerActive(String name, boolean active) {
        for (GenericMapRenderer renderer : rendererList) {
            renderer.setLayerActive(name, active);
        }
    }
}
