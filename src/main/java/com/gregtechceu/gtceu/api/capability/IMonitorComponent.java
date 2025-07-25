package com.gregtechceu.gtceu.api.capability;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;

public interface IMonitorComponent {

    boolean isMonitor();

    ResourceTexture getComponentIcon();

    BlockPos getPos();
}
