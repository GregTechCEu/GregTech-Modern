package com.gregtechceu.gtceu.api.capability;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface IMiner {


    boolean drainEnergy(boolean simulate);

    default boolean drainFluid(boolean simulate) {
        return true;
    }

    boolean isInventoryFull();

    void setInventoryFull(boolean isFull);

    static int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
