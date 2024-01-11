package com.gregtechceu.gtceu.utils;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;

import java.util.List;

/**
 * Dummy machine used for searching recipes outside of a machine.
 */
public class DummyRecipeLogicMachine extends WorkableTieredMachine implements IRecipeLogicMachine {
    public DummyRecipeLogicMachine(IMachineBlockEntity be, int tier, Int2LongFunction tankScalingFunction, Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy, Object... args) {
        super(be, tier, tankScalingFunction, args);
        reinitializeCapabilities(capabilitiesProxy);
    }

    public void reinitializeCapabilities(Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> caps) {
        this.capabilitiesProxy.clear();
        this.capabilitiesProxy.putAll(caps);
    }
}
