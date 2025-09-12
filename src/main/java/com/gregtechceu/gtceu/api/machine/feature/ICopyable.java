package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ICopyable {

    void copyConfig(CompoundTag nbt);

    void pasteConfig(CompoundTag nbt);

    Set<ItemStack> getItemsRequiredForPaste(CompoundTag nbt);

    default @Nullable Component getConfigTooltip(CompoundTag nbt) {
        return null;
    };
}
