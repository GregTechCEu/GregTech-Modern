package com.gregtechceu.gtceu.api.capability.data;

/**
 * Used for {@link gregtech.api.capability.impl.ComputationRecipeLogic}
 */
public interface IComputationUser {

    /**
     * Called to request CWU for recipe logic.
     * 
     * @param requested the requested CWU
     * @param simulate  whether to simulate the request
     * @return the amount of CWU supplied.
     */
    long requestCWU(long requested, boolean simulate);
}
