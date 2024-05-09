package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machines.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machines.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.common.machines.trait.miner.MinerLogic;

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
