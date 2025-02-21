package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.layer.MapRenderLayer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * A map renderer designed to work with any map mod.
 */
@OnlyIn(Dist.CLIENT)
public abstract class GenericMapRenderer {

    protected List<MapRenderLayer> layers;

    public GenericMapRenderer() {
        this(true);
    }

    public GenericMapRenderer(boolean initializeLayers) {
        layers = new ArrayList<>();
        if (initializeLayers) {
            Layers.addLayersTo(layers, this);
        }
    }

    public abstract boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos,
                                      ProspectorMode.FluidInfo fluid);

    public abstract boolean addMarker(String name, ResourceKey<Level> dim, GeneratedVeinMetadata vein, String id);

    public abstract boolean removeMarker(ResourceKey<Level> dim, String id);

    public abstract boolean doShowLayer(String name);

    public abstract void setLayerActive(String name, boolean active);

    public abstract void clear();
}
