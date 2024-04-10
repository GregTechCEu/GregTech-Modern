package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;

import org.jetbrains.annotations.Nullable;

/**
 * Implement this interface in order to make a BlockEntity into a block that recieves a cleanroom from other blocks
 */
public interface ICleanroomReceiver {

    /**
     *
     * @return the cleanroom the machine is receiving from
     */
    @Nullable
    ICleanroomProvider getCleanroom();

    /**
     * sets the machine's cleanroom to the provided one
     *
     * @param provider the cleanroom to assign to this machine
     */
    void setCleanroom(ICleanroomProvider provider);
}
