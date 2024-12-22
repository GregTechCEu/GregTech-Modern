package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class EfficiencyModifier implements RecipeModifier {

    private final double baseMultiplier;
    private final double efficiency;
    private final double hardCap;

    private EfficiencyModifier(double baseMultiplier, double efficiency, double hardCap) {
        Preconditions.checkArgument(baseMultiplier > 0, "Base multiplier must be > 0: %s", baseMultiplier);
        Preconditions.checkArgument(efficiency > 0, "Efficiency must be > 0: %s", efficiency);
        Preconditions.checkArgument(hardCap >= 0, "Hard cap must be >= 0: %s", hardCap);
        this.baseMultiplier = baseMultiplier;
        this.efficiency = efficiency;
        this.hardCap = hardCap;
    }

    /**
     * Creates an Efficiency Modifier with the given parameters
     * 
     * @param baseMultiplier base duration multiplier to be applied to the recipe
     * @param efficiency     multiplier to be applied per consecutive recipe run
     * @param hardCap        limit on how low the duration can be multiplied
     * @return Efficiency Modifier
     */
    public static EfficiencyModifier of(double baseMultiplier, double efficiency, double hardCap) {
        return new EfficiencyModifier(baseMultiplier, efficiency, hardCap);
    }

    public static EfficiencyModifier of(double baseMultiplier, double efficiency) {
        return of(baseMultiplier, efficiency, 0.25);
    }

    public static EfficiencyModifier of(double efficiency) {
        return of(2, efficiency, 0.25);
    }

    /**
     * Efficiency recipe modifier
     * <p>
     * Duration will be multiplied by <code>base × efficiency<sup>runs</sup></code>
     * 
     * @param machine an {@link IRecipeLogicMachine}
     * @param recipe  recipe
     * @return Efficiency Modifier
     */
    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IRecipeLogicMachine rlm)) {
            return RecipeModifier.nullWrongType(IRecipeLogicMachine.class, machine);
        }
        int runs = rlm.getRecipeLogic().getConsecutiveRecipes();
        return ModifierFunction.builder()
                .durationMultiplier(Math.max(hardCap, baseMultiplier * Math.pow(efficiency, runs)))
                .build();
    }
}
