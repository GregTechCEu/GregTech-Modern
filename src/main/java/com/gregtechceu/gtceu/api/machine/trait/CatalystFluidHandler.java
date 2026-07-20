package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class CatalystFluidHandler extends NotifiableFluidTank {

    public CatalystFluidHandler(MetaMachine machine, int slots, int capacity, IO io, IO capabilityIO) {
        super(machine, slots, capacity, io, capabilityIO);
    }

    @Override
    public boolean handleRecipe(IO io, GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
        List<FluidIngredient> nonConsume = new ArrayList<>();
        List<FluidIngredient> consumable = new ArrayList<>();
        for (var ingredient : left) {
            if (ingredient.getChance() == 0) {
                nonConsume.add(ingredient);
            } else {
                consumable.add(ingredient);
            }
        }
        super.handleRecipe(io, recipe, nonConsume, simulate);
        left.clear();
        left.addAll(nonConsume);
        left.addAll(consumable);
        return left.isEmpty();
    }
}
