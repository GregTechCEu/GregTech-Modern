package com.gregtechceu.gtceu.api.capability;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public interface IMonitorComponent {

    default boolean isMonitor() {
        return false;
    }

    Object getComponentIcon();

    BlockPos getBlockPos();

    default @Nullable IItemHandler getDataItems() {
        return null;
    }
}
