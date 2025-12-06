package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface ICopyable {

    default CompoundTag saveCopyConfig(CompoundTag tag) {
        return tag;
    }

    default void loadCopyConfig(ServerPlayer player, CompoundTag tag) {}

    default void getItemsRequiredToPaste(CompoundTag tag) {}
}
