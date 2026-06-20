package com.gregtechceu.gtceu.api.machine.trait.multiblock;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import java.util.List;

/**
 * A machine trait that is specific to multiblock controllers.
 */
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

    /**
     * Called when the multiblock structure is formed
     * 
     * @see MultiblockControllerMachine#onStructureFormed()
     */
    public void onStructureFormed() {}

    /**
     * Called when the multiblock structure becomes invalid
     * 
     * @see MultiblockControllerMachine#onStructureInvalid()
     */
    public void onStructureInvalid() {}
}
