package com.gregtechceu.gtceu.common.machine.trait;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class DummyRecipeCapabilityHolder extends MetaMachine implements IRecipeCapabilityHolder {

    private final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> table;

    public DummyRecipeCapabilityHolder(IMachineBlockEntity holder) {
        super(holder);
        this.holder.getRootStorage().detach(getSyncStorage());
        this.table = Tables.newCustomTable(new EnumMap<>(IO.class), HashMap::new);
    }

    public void addCapability(IO io, RecipeCapability<?> capability, List<IRecipeHandler<?>> handlers) {
        table.put(io, capability, handlers);
    }

    @NotNull
    @Override
    public Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
        return table;
    }
}
