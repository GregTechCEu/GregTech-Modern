package com.gregtechceu.gtceu.api.capability;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public interface IMonitorComponent {

    default boolean isMonitor() {
        return false;
    }

    IGuiTexture getComponentIcon();

    BlockPos getPos();

    default @Nullable IItemHandler getDataItems() {
        return null;
    }
}
