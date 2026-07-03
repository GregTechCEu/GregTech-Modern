package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;

public interface ICoilMachine extends ITieredMachine, IOverclockMachine {

    int getCoilTier();

    ICoilType getCoilType();
}
