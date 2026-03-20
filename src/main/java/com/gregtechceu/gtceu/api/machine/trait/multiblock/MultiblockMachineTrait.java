package com.gregtechceu.gtceu.api.machine.trait.multiblock;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

public abstract class MultiblockMachineTrait extends MachineTrait {

    public MultiblockMachineTrait(MultiblockControllerMachine multiMachine) {
        super(multiMachine);
    }

    @Override
    public MultiblockControllerMachine getMachine() {
        return (MultiblockControllerMachine)super.getMachine();
    }

    public void onStructureFormed() {}

    public void onStructureInvalid() {}
}
