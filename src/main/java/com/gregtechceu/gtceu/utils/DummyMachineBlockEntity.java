package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;

import java.util.List;

/**
 * Dummy machine BE used for wrapping {@link DummyRecipeLogicMachine}s
 */
public class DummyMachineBlockEntity implements IMachineBlockEntity {

    @Getter
    public final DummyRecipeLogicMachine metaMachine;
    @Getter
    private final MachineDefinition definition;

    public DummyMachineBlockEntity(int tier, GTRecipeType type, Int2IntFunction tankScalingFunction,
                                   Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy,
                                   Object... args) {
        this.definition = MachineDefinition.createDefinition(GTCEu.id("dummy"));
        this.definition.setRecipeTypes(new GTRecipeType[] { type });
        this.definition.setTier(tier);

        this.metaMachine = new DummyRecipeLogicMachine(this, tier, tankScalingFunction, capabilitiesProxy, args);
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public MultiManagedStorage getRootStorage() {
        return null;
    }
}
