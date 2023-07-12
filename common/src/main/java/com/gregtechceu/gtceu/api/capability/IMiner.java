package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;

public interface IMiner extends IRecipeLogicMachine {

    @Override
    MinerLogic getRecipeLogic();

    boolean drainInput(boolean simulate);

    default int getPipeLength() {
        return getRecipeLogic().getPipeLength();
    }

    static int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
