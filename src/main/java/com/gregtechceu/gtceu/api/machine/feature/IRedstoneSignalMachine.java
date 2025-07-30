package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

public interface IRedstoneSignalMachine extends IMachineFeature {

    default int getOutputSignal(@Nullable Direction side) {
        return 0;
    }

    default int getOutputDirectSignal(Direction direction) {
        return 0;
    }

    default int getAnalogOutputSignal() {
        return 0;
    }

    default boolean canConnectRedstone(Direction side) {
        return false;
    }

    /**
     * Call to update output signal.
     * also see {@link IRedstoneSignalMachine#getOutputSignal(Direction)} and
     * {@link IRedstoneSignalMachine#getOutputDirectSignal(Direction)}
     */
    default void updateSignal() {
        var level = self().getLevel();
        if (!level.isClientSide) {
            self().notifyBlockUpdate();
        }
    }
}
