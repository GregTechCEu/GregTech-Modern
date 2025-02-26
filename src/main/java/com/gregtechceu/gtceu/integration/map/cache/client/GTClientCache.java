package com.gregtechceu.gtceu.integration.map.cache.client;

import com.gregtechceu.gtceu.GTCEu;
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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

public class GTClientCache extends WorldCache implements IClientCache {

    public static final GTClientCache instance = new GTClientCache();

    private final FluidCache fluids = new FluidCache();

    public void notifyNewVeins(List<GeneratedVeinMetadata> veins) {
        if (veins.isEmpty()) return;

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        int amount = veins.size();
        player.sendSystemMessage(Component.translatable("message.gtceu.new_veins.amount", amount));

        for (GeneratedVeinMetadata vein : veins) {
            if (vein == null) return;

            String veinId = vein.id().toString();
            String veinLoc = vein.originChunk().toString();
            int rgb = vein.definition().veinGenerator().getAllMaterials().get(0).getMaterialRGB();

            MutableComponent veinName = Component.translatable(veinId.replace("gtceu:", "gtceu.jei.ore_vein."));

            if (vein.definition().veinGenerator().getAllMaterials().get(0) != null) {
                veinName.setStyle(veinName.getStyle().withColor(rgb));
            }

            player.sendSystemMessage(Component.translatable("message.gtceu.new_veins.name", veinName, veinLoc));

            GTCEu.LOGGER.info("Vein name is {}", veinName);
            GTCEu.LOGGER.info("Vein color is {}", rgb);
        }
    }

    public void addFluid(ResourceKey<Level> dim, int chunkX, int chunkZ, ProspectorMode.FluidInfo fluid) {
        fluids.addFluid(dim, chunkX, chunkZ, fluid);
    }

    @Override
    public boolean addVein(ResourceKey<Level> dim, int gridX, int gridZ, GeneratedVeinMetadata vein) {
        GenericMapRenderer renderer = GroupingMapRenderer.getInstance();
        if (renderer != null) {
            renderer.addMarker(OreRenderLayer.getName(vein).getString(), dim, vein, OreRenderLayer.getId(vein));
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
        return cache.get(dim).toNBT(true);
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
        cache.get(dim).fromNBT(data, true);

        // FIXME janky hack mate
        GenericMapRenderer renderer = GroupingMapRenderer.getInstance();
        if (renderer != null) {
            for (GridCache grid : cache.get(dim).getCache().values()) {
                for (GeneratedVeinMetadata vein : grid.getVeins()) {
                    renderer.addMarker(OreRenderLayer.getName(vein).getString(), dim, vein, OreRenderLayer.getId(vein));
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