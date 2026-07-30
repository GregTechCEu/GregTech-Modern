package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.ItemIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// only handles non-consumable ingredient
// TODO: fix this
public class CatalystItemHandler extends NotifiableItemStackHandler {

    public CatalystItemHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO) {
        super(machine, slots, handlerIO, capabilityIO);
    }

    @Override
    public boolean handleRecipe(IO io, GTRecipe recipe, List<ItemIngredient> left, boolean simulate) {
        List<ItemIngredient> nonConsume = new ArrayList<>();
        List<ItemIngredient> consumable = new ArrayList<>();
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
