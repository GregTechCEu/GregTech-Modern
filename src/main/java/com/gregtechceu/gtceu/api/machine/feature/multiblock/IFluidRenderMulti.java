package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IFluidRenderMulti extends IWorkableMultiController, IMachineFeature {

    @ApiStatus.NonExtendable
    default Set<BlockPos> getFluidOffsets() {
        Set<BlockPos> offsets = getFluidBlockOffsets();
        if (offsets.isEmpty() && this.isFormed()) {
            offsets = saveOffsets();
            setFluidBlockOffsets(offsets);
        }
        return offsets;
    }

    @ApiStatus.OverrideOnly
    @NotNull
    Set<BlockPos> getFluidBlockOffsets();

    @ApiStatus.Internal
    void setFluidBlockOffsets(@NotNull Set<BlockPos> offsets);

    @Override
    default void onStructureFormed() {
        saveOffsets();
    }

    @Override
    default void onStructureInvalid() {
        getFluidBlockOffsets().clear();
    }

    @NotNull
    Set<BlockPos> saveOffsets();
}
