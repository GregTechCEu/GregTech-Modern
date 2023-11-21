package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;

import java.util.Map;

public class MaceratorMachine extends SimpleTieredMachine {
    public MaceratorMachine(IMachineBlockEntity holder, int tier, Int2LongFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
    }

    @Override
    public int getOutputLimit(RecipeCapability<?> capability) {
        return capability == ItemRecipeCapability.CAP && getTier() < GTValues.HV ? 0 : super.getOutputLimit(capability);
    }

    @Override
    public Map<RecipeCapability<?>, Integer> getOutputLimits() {
        return Map.of(ItemRecipeCapability.CAP, switch (getTier()) {
            case 0, 1, 2 -> 1;
            case 3 -> 3;
            default -> 4;
        });
    }
}
