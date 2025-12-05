package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface ICopyable {

    default CompoundTag gatherConfig(CompoundTag tag) {
        return tag;
    }

    default void loadConfigTag(ServerPlayer player, CompoundTag tag) {}

    default void getItemsRequiredToPaste(CompoundTag tag) {
    }

}
