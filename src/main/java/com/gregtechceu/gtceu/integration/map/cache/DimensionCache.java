package com.gregtechceu.gtceu.integration.map.cache;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionCache {

    @Getter
    private final Map<GridPos, GridCache> cache = new HashMap<>();

    public boolean dirty;

    public boolean addVein(int gridX, int gridZ, GeneratedVeinMetadata vein) {
        GridPos key = new GridPos(gridX, gridZ);
        if (!cache.containsKey(key)) {
            cache.put(key, new GridCache());
        }
        boolean added = cache.get(key).addVein(vein);
        dirty = added || dirty;
        return added;
    }

    public CompoundTag toNBT(boolean saveDepleted) {
        return toNBT(new CompoundTag(), saveDepleted);
    }

    public CompoundTag toNBT(CompoundTag nbt) {
        return toNBT(nbt, false);
    }

    public CompoundTag toNBT(CompoundTag nbt, boolean saveDepleted) {
        for (GridPos key : cache.keySet()) {
            nbt.put(key.x + "," + key.z, cache.get(key).toNBT(saveDepleted));
        }
        return nbt;
    }

    public void fromNBT(CompoundTag tag) {
        for (String gridPos : tag.getAllKeys()) {
            String[] split = gridPos.split(",");
            GridPos key = new GridPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            if (!cache.containsKey(key)) {
                cache.put(key, new GridCache());
            }
            cache.get(key).fromNBT(tag.getList(gridPos, Tag.TAG_COMPOUND));
        }
    }

    public List<GeneratedVeinMetadata> getNearbyVeins(BlockPos pos, int blockRadius) {
        return getVeinsInBounds(pos.offset(-blockRadius, 0, -blockRadius), pos.offset(blockRadius, 0, blockRadius));
    }

    public List<GeneratedVeinMetadata> getVeinsInBounds(BlockPos topLeftBlock, BlockPos bottomRightBlock) {
        GridPos topLeft = new GridPos(topLeftBlock);
        GridPos bottomRight = new GridPos(bottomRightBlock);
        List<GeneratedVeinMetadata> found = new ArrayList<>();
        for (int i = topLeft.x; i <= bottomRight.x; i++) {
            for (int j = topLeft.z; j <= bottomRight.z; j++) {
                GridPos curPos = new GridPos(i, j);
                if (cache.containsKey(curPos)) {
                    found.addAll(cache.get(curPos)
                            .getVeinsMatching(vein -> vein.center().getX() >= topLeftBlock.getX() &&
                                    vein.center().getX() <= bottomRightBlock.getX() &&
                                    vein.center().getZ() >= topLeftBlock.getZ() &&
                                    vein.center().getZ() <= bottomRightBlock.getZ()));
                }
            }
        }
        return found;
    }

    public List<GeneratedVeinMetadata> getVeinsInChunk(ChunkPos pos) {
        GridPos gPos = new GridPos(pos);
        if (cache.containsKey(gPos)) {
            return cache.get(gPos).getVeinsMatching(vein -> pos.equals(vein.originChunk()));
        }
        return new ArrayList<>();
    }

    public void removeAllInChunk(ChunkPos pos) {
        GridPos gPos = new GridPos(pos);
        if (cache.containsKey(gPos)) {
            cache.get(gPos).removeVeinsMatching(vein -> pos.equals(vein.originChunk()));
        }
    }
}
