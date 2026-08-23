package com.gregtechceu.gtceu.api.multiblock.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class AutobuildHelper {

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
}
