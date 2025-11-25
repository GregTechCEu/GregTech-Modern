package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICopyable {

    default CompoundTag gatherConfig(CompoundTag tag) {
        return tag;
    }

    default void loadConfigTag(CompoundTag tag) {}

    default List<ItemStack> getItemsRequiredToCopy(List<ItemStack> items) {
        return items;
    }

}
