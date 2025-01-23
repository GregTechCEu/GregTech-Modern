package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Dummy machine used for searching recipes outside of a machine.
 */
public class DummyRecipeLogicMachine extends WorkableTieredMachine implements IRecipeLogicMachine {

    public DummyRecipeLogicMachine(IMachineBlockEntity be, int tier, Int2IntFunction tankScalingFunction,
                                   Map<IO, List<RecipeHandlerList>> capabilitiesProxy,
                                   Object... args) {
        super(be, tier, tankScalingFunction, args);
        reinitializeCapabilities(capabilitiesProxy);
    }

    public void reinitializeCapabilities(Map<IO, List<RecipeHandlerList>> caps) {
        this.capabilitiesProxy.clear();
        this.capabilitiesFlat.clear();

        this.capabilitiesProxy.putAll(caps);
    }

    public void reinitializeHandlers(Collection<RecipeHandlerList> handlers) {
        this.capabilitiesProxy.clear();
        this.capabilitiesFlat.clear();
        for (RecipeHandlerList handlerList : handlers) {
            this.addHandlerList(handlerList);
        }
    }
}
