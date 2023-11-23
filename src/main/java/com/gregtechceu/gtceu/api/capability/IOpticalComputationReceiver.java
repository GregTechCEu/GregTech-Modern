package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.common.machine.trait.computation.ComputationRecipeLogic;

/**
 * Used in conjunction with {@link ComputationRecipeLogic}.
 */
public interface IOpticalComputationReceiver {

    IOpticalComputationProvider getComputationProvider();
}