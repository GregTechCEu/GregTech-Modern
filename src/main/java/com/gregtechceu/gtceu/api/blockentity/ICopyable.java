package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICopyable {

    default CompoundTag gatherConfig(CompoundTag tag) {
        return tag;
    }

    default void loadConfigTag(ServerPlayer player, CompoundTag tag) {}

    default List<ItemStack> getItemsRequiredToPaste(CompoundTag tag, List<ItemStack> items) {
        return items;
    }

}
