package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

public interface IWorkableMultiController extends IRecipeLogicMachine {

    MultiblockControllerMachine self();
}
