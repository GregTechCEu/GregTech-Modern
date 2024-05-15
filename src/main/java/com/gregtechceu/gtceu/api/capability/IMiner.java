package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;

public interface IMiner extends IRecipeLogicMachine, IMachineLife {

    @Override
    MinerLogic getRecipeLogic();

    @Override
    default void onMachineRemoved() {
        getRecipeLogic().onRemove();
    }

    boolean drainInput(boolean simulate);

    static int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
