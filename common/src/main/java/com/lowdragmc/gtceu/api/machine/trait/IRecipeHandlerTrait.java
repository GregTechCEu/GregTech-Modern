package com.lowdragmc.gtceu.api.machine.trait;

import com.lowdragmc.gtceu.api.capability.recipe.IO;
import com.lowdragmc.gtceu.api.capability.recipe.IRecipeHandler;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote IRecipeHandlerTrait
 */
public interface IRecipeHandlerTrait<K> extends IRecipeHandler<K> {
    IO getHandlerIO();

    /**
     * add listener for notification when it changed.
     */
    ISubscription addChangedListener(Runnable listener);
}
