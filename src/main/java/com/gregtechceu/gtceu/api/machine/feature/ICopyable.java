package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ICopyable {

    void copyConfig(CompoundTag nbt);

    void pasteConfig(CompoundTag nbt);

    Map<Item, Integer> getItemsRequiredForPaste(CompoundTag nbt);

    default @Nullable Component getConfigTooltip(CompoundTag nbt) {
        return null;
    };
}
