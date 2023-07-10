package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

public interface IMiner extends IRecipeLogicMachine {

    void setRecipeType(GTRecipeType type);

    boolean drainEnergy(boolean simulate);

    default boolean drainFluid(boolean simulate) {
        return true;
    }

    boolean isInventoryFull();

    void setInventoryFull(boolean isFull);

    static int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
