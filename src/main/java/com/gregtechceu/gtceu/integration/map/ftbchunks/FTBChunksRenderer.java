package com.gregtechceu.gtceu.integration.map.ftbchunks;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid.FluidChunkHighlighter;
import com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore.OreVeinElement;
import com.mojang.blaze3d.platform.NativeImage;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.client.map.MapManager;
import dev.ftb.mods.ftblibrary.ui.Widget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import journeymap.client.api.display.Overlay;
import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FTBChunksRenderer extends GenericMapRenderer {
    public static final Table<ResourceKey<Level>, String, OreVeinElement> oreElements = HashBasedTable.create();
    public static final Table<ResourceKey<Level>, ChunkPos, ProspectorMode.FluidInfo> fluidElements = HashBasedTable
            .create();

    @Getter
    private static final Map<String, Widget> markers = new Object2ObjectOpenHashMap<>();
    
    @Override
    public boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos, ProspectorMode.FluidInfo fluid) {
        fluidElements.put(dim, pos, fluid);
        if (MapManager.getInstance().isPresent()) {
            var mapManager = MapManager.getInstance().get();
            var mapDimension = mapManager.getDimension(dim);
            mapDimension.get
            new FluidChunkHighlighter()
        }
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
        return FTBChunksIntegration.getOptions().showLayer(name);
    }

    @Override
    public void setLayerActive(String name, boolean active) {
        FTBChunksIntegration.getOptions().toggleLayer(name, active);
    }
}
