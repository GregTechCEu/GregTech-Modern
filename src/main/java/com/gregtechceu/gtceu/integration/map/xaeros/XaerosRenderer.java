package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore.OreVeinElement;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class XaerosRenderer extends GenericMapRenderer {

    public static final Table<ResourceKey<Level>, String, OreVeinElement> oreElements = HashBasedTable.create();
    public static final Table<ResourceKey<Level>, ChunkPos, ProspectorMode.FluidInfo> fluidElements = HashBasedTable
            .create();

    public XaerosRenderer() {
        super();
    }

    @Override
    public boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos,
                             ProspectorMode.FluidInfo fluid) {
        fluidElements.put(dim, pos, fluid);
        return true;
    }

    @Override
    public boolean addMarker(String name, ResourceKey<Level> dim, GeneratedVeinMetadata vein, String id) {
        oreElements.put(dim, id, new OreVeinElement(vein, name));
        return true;
    }

    @Override
    public boolean removeMarker(ResourceKey<Level> dim, String id) {
        OreVeinElement marker = oreElements.remove(dim, id);
        return marker != null;
    }

    @Override
    public boolean doShowLayer(String name) {
        return XaerosMapPlugin.getOptionValue(name);
    }

    @Override
    public void setLayerActive(String name, boolean active) {
        XaerosMapPlugin.toggleOption(name, active);
    }

    @Override
    public void clear() {
        oreElements.clear();
        fluidElements.clear();
    }
}
