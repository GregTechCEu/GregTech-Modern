package com.gregtechceu.gtceu.api.multiblock.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class AutobuildHelper {

    // todo remove
    public static Long2ObjectMap<BlockInfo> readBlockPreferences(CompoundTag tag) {
        Long2ObjectMap<BlockInfo> blockPreferences = new Long2ObjectOpenHashMap<>();
        if (!tag.contains("blockPreferences", CompoundTag.TAG_LIST)) {
            return blockPreferences;
        }

        var preferences = tag.getList("blockPreferences", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < preferences.size(); i++) {
            CompoundTag preference = preferences.getCompound(i);
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                    preference.getCompound("state"));
            if (state.isAir()) continue;
            blockPreferences.put(preference.getLong("pos"), BlockInfo.fromBlockState(state));
        }
        return blockPreferences;
    }

    /*
     * iterate over every position in the structure
     * for each block
     * - if that block is part of the structure and valid in that predicate, add to alreadyPlaced
     * - if that block can be replaced(air, tall grass, etc? block property replaceable) add to replaceableBlocks
     * - if that block CANT be replaced and not valid, add to canNotPlace,
     * maybe add what already exists there to another list for reporting(invalidBlocks)?
     * 
     * 
     * for each replaceableBlock
     * - if that candidate from the predicate exists in the inventory, add to some blocksToRemove list
     * (small size for chunked building)
     * figure out the auto placement action
     * - if the candidate does not exist, add to blocksMissing(for later reporting)
     */
}
