package com.gregtechceu.gtceu.api.machine.trait.feature;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

// Trait for adding custom redstone signal outputs
public interface IRedstoneSignalTrait {

    /**
     * Gets the redstone output signal at a specific side
     *
     * @param side Side
     * @return Output signal
     */
    default int getOutputSignal(@Nullable Direction side) {
        return 0;
    }

    /**
     * Gets the direct output signal at a specific side
     *
     * @param side Side
     * @return Direct output signal
     */
    default int getOutputDirectSignal(@Nullable Direction side) {
        return 0;
    }

    /**
     * Gets the analog (comparator) output signal
     *
     * @return Analog output signal.
     */
    default int getAnalogOutputSignal() {
        return 0;
    }

    /**
     * Returns if redstone can be connected to a specific side of this machine
     *
     * @param side The side to check
     * @return If redstone can be connected
     */
    default boolean canConnectRedstone(Direction side) {
        return false;
    }
}
