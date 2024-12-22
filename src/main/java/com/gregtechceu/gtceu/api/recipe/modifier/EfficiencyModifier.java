package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

public class EfficiencyModifier implements RecipeModifier {

    private final double baseMultiplier;
    private final double efficiency;

    private EfficiencyModifier(double baseMultiplier, double efficiency) {
        this.baseMultiplier = baseMultiplier;
        this.efficiency = efficiency;
    }

    public static EfficiencyModifier of(double baseMultiplier, double efficiency) {
        return new EfficiencyModifier(baseMultiplier, efficiency);
    }

    public static EfficiencyModifier of(double efficiency) {
        return new EfficiencyModifier(2, efficiency);
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            return RecipeModifier.nullWrongType(IRecipeLogicMachine.class, machine);
        }
        return ModifierFunction.builder()
                .durationMultiplier(baseMultiplier * Math.pow(efficiency, rlm.getRecipeLogic().getConsecutiveRecipes()))
                .build();
    }
}
