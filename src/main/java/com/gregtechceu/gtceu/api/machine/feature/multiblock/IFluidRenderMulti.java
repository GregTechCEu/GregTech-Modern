package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface IFluidRenderMulti extends IWorkableMultiController, IMachineFeature {

    Set<BlockPos> getFluidBlockOffsets();

    @Override
    default void onStructureFormed() {
        saveOffsets();
    }

    @Override
    default void onStructureInvalid() {
        getFluidBlockOffsets().clear();
    }

    void saveOffsets();
}
