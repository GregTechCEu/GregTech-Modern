package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

public interface IRecipeHandlerTrait<K> extends IRecipeHandler<K> {

    IO getHandlerIO();

    /**
     * add listener for notification when it changed.
     */
    ISubscription addChangedListener(Runnable listener);
}
