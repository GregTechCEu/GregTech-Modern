package com.gregtechceu.gtceu.integration.map.cache.fluid;

import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FluidCache {

    private final Table<ResourceKey<Level>, ChunkPos, ProspectorMode.FluidInfo> fluidCache = HashBasedTable.create();

    public void addFluid(ResourceKey<Level> dim, int chunkX, int chunkZ, ProspectorMode.FluidInfo fluid) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (!fluidCache.contains(dim, pos)) {
            fluidCache.put(dim, pos, fluid);
            GroupingMapRenderer.getInstance().addMarker(FluidRenderLayer.getName(fluid).getString(),
                    FluidRenderLayer.getId(fluid, pos), dim, pos, fluid);
        }
    }

    public void fromNbt(CompoundTag nbt) {
        var fluidList = nbt.getListOrEmpty("fluids");
        for (var fluidTagRaw : fluidList) {
            if (fluidTagRaw instanceof CompoundTag fluidTag) {
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
                        Identifier.parse(fluidTag.getStringOr("dim", "")));
                ChunkPos pos = ChunkPos.unpack(fluidTag.getLongOr("pos", 0L));
                var fluid = ProspectorMode.FluidInfo.fromNbt(fluidTag);
                fluidCache.put(dim, pos, fluid);

                GroupingMapRenderer.getInstance().addMarker(FluidRenderLayer.getName(fluid).getString(),
                        FluidRenderLayer.getId(fluid, pos), dim, pos, fluid);
            }
        }
    }

    public CompoundTag toNbt() {
        var result = new CompoundTag();
        var fluidList = new ListTag();
        for (var dimensions : fluidCache.rowMap().entrySet()) {
            for (var entry : dimensions.getValue().entrySet()) {
                CompoundTag tag = entry.getValue().toNbt();
                tag.putLong("pos", entry.getKey().pack());
                tag.putString("dim", dimensions.getKey().identifier().toString());
                fluidList.add(tag);
            }
        }
        result.put("fluids", fluidList);
        return result;
    }

    public void clear() {
        fluidCache.clear();
    }
}
