package com.gregtechceu.gtceu.api.capability;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.core.BlockPos;
import net.minecraftforge.items.IItemHandler;

public interface IMonitorComponent {

    boolean isMonitor();

    IGuiTexture getComponentIcon();

    BlockPos getPos();

    default IItemHandler getDataItems() {
        return null;
    }
}
