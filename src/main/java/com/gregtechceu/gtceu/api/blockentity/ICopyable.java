package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/// An interface for machines and machine traits which have settings that can be copied using the machine memory card.
public interface ICopyable {

    /// Saves the current config into a CompoundTag.
    default CompoundTag copyConfig(CompoundTag tag) {
        return tag;
    }

    /// Loads a saved config from a CompoundTag and applies it to an existing object.
    default void pasteConfig(ServerPlayer player, CompoundTag tag) {}

    /// Returns a `List<ItemStack>` of items required to paste the saved config.
    default List<ItemStack> getItemsRequiredToPaste() {
        return List.of();
    }
}
