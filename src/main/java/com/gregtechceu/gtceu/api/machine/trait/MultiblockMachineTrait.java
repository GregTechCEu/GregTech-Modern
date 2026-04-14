package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

public abstract class MultiblockMachineTrait extends MachineTrait {

    public MultiblockMachineTrait(MultiblockControllerMachine multiMachine) {
        super(multiMachine);
    }

    public void onStructureFormed() {}

    public void onStructureInvalid() {}
}
