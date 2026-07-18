package com.gregtechceu.gtceu.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraftforge.items.IItemHandler;

import brachy.modularui.api.drawable.IDrawable;
import org.jetbrains.annotations.Nullable;

public interface IMonitorComponent {

    default boolean isMonitor() {
        return false;
    }

    IDrawable getIcon();

    BlockPos getBlockPos();

    default @Nullable IItemHandler getDataItems() {
        return null;
    }
}
