package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.xaeros.minimap.OreVeinElement;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public class XaerosRenderer extends GenericMapRenderer {

    public static final Map<String, OreVeinElement> oreElements = new Object2ObjectOpenHashMap<>();
    public static final Map<ChunkPos, ProspectorMode.FluidInfo> fluidElements = new Object2ObjectOpenHashMap<>();

    public XaerosRenderer() {
        super();
    }

    @Override
    public boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos, ProspectorMode.FluidInfo fluid) {
        fluidElements.put(pos, fluid);
        return true;
    }

    @Override
    public boolean addMarker(String name, String id, GeneratedVeinMetadata vein) {
        oreElements.put(id, new OreVeinElement(vein, name));
        return true;
    }

    @Override
    public boolean removeMarker(String id) {
        OreVeinElement marker = oreElements.remove(id);
        return marker != null;
    }

    @Override
    public boolean doShowLayer(String name) {
        return XaerosWorldMapPlugin.getOptionValue(name);
    }

    @Override
    public void setLayerActive(String name, boolean active) {
        XaerosWorldMapPlugin.toggleOption(name, active);
    }
}
