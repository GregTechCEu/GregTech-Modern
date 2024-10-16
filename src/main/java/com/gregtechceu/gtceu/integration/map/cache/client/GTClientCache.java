package com.gregtechceu.gtceu.integration.map.cache.client;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.cache.DimensionCache;
import com.gregtechceu.gtceu.integration.map.cache.GridCache;
import com.gregtechceu.gtceu.integration.map.cache.WorldCache;
import com.gregtechceu.gtceu.integration.map.cache.fluid.FluidCache;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class GTClientCache extends WorldCache implements IClientCache {

    public static final GTClientCache instance = new GTClientCache();

    private final FluidCache fluids = new FluidCache();

    public void notifyNewVeins(int amount) {
        if (amount <= 0) return;
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.gtceu.new_veins", amount));
    }

    public void addFluid(ResourceKey<Level> dim, int chunkX, int chunkZ, ProspectorMode.FluidInfo fluid) {
        fluids.addFluid(dim, chunkX, chunkZ, fluid);
    }

    @Override
    public boolean addVein(ResourceKey<Level> dim, int gridX, int gridZ, GeneratedVeinMetadata vein) {
        GenericMapRenderer renderer = GroupingMapRenderer.getInstance();
        if (renderer != null) {
            renderer.addMarker(OreRenderLayer.getName(vein).getString(), OreRenderLayer.getId(vein), vein);
        }
        return super.addVein(dim, gridX, gridZ, vein);
    }

    @Override
    public Collection<ResourceKey<Level>> getExistingDimensions(String prefix) {
        return cache.keySet();
    }

    @Override
    public CompoundTag saveDimFile(String prefix, ResourceKey<Level> dim) {
        if (!cache.containsKey(dim)) return null;
        return cache.get(dim).toNBT();
    }

    @Override
    public CompoundTag saveSingleFile(String name) {
        return fluids.toNbt();
    }

    @Override
    public void readDimFile(String prefix, ResourceKey<Level> dim, CompoundTag data) {
        if (!cache.containsKey(dim)) {
            cache.put(dim, new DimensionCache());
        }
        cache.get(dim).fromNBT(data);

        // FIXME janky hack mate
        GenericMapRenderer renderer = GroupingMapRenderer.getInstance();
        if (renderer != null) {
            for (GridCache grid : cache.get(dim).getCache().values()) {
                for (GeneratedVeinMetadata vein : grid.getVeins()) {
                    renderer.addMarker(OreRenderLayer.getName(vein).getString(), OreRenderLayer.getId(vein), vein);
                }
            }
        }
    }

    @Override
    public void readSingleFile(String name, CompoundTag data) {
        fluids.fromNbt(data);
    }

    @Override
    public void setupCacheFiles() {
        addDimFiles();
        addSingleFile("fluids");
    }

    @Override
    public void clear() {
        super.clear();
        fluids.clear();
    }
}
