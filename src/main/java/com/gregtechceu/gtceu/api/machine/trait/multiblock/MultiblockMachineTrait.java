package com.gregtechceu.gtceu.api.machine.trait.multiblock;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import java.util.List;

public abstract class MultiblockMachineTrait extends MachineTrait {

    public MultiblockMachineTrait() {
        super();
    }

    @Override
    public MultiblockControllerMachine getMachine() {
        return (MultiblockControllerMachine) super.getMachine();
    }

    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(MultiblockControllerMachine.class);
    }

    public void onStructureFormed() {}

    public void onStructureInvalid() {}
}
