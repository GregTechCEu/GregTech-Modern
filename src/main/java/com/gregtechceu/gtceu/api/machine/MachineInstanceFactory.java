package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

@FunctionalInterface
public interface MachineInstanceFactory {

    MetaMachine buildMachine(BlockEntityCreationInfo info);

    @FunctionalInterface
    interface Tiered {
        MetaMachine buildMachine(BlockEntityCreationInfo info, int tier);
    }
}
